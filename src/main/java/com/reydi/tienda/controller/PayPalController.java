package com.reydi.tienda.controller;

import com.reydi.tienda.model.EstadoPedido;

import com.reydi.tienda.model.Pago;
import com.reydi.tienda.model.Pedido;
import com.reydi.tienda.service.PagoService;
import com.reydi.tienda.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/pagos/paypal")
@RequiredArgsConstructor
public class PayPalController {

    private final PedidoService pedidoService;
    private final PagoService pagoService;

    @Value("${paypal.client-id}")
    private String paypalClientId;

    @Value("${paypal.client-secret}")
    private String paypalClientSecret;

    @Value("${paypal.mode}")
    private String paypalMode;

    private static final String PAYPAL_API_BASE = "https://api-m.sandbox.paypal.com"; // Para sandbox

    /**
     * Crear orden en PayPal
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
            Integer pedidoId = (Integer) request.get("pedidoId");
            Double monto = (Double) request.get("monto");
            String moneda = (String) request.get("moneda");
            // Asegurar que la moneda sea USD
            if (!"USD".equals(moneda)) {
                moneda = "USD";
            }


            if (pedidoId == null || monto == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "pedidoId y monto son requeridos"));
            }

            Pedido pedido = pedidoService.buscarPorId(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            // Obtener token de acceso de PayPal
            String accessToken = getPayPalAccessToken();

            // Crear orden en PayPal
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> orderRequest = new HashMap<>();
            orderRequest.put("intent", "CAPTURE");

            Map<String, Object> purchaseUnit = new HashMap<>();
            Map<String, Object> amount = new HashMap<>();
            amount.put("currency_code", moneda != null ? moneda : "PEN");
            amount.put("value", String.format("%.2f", monto));
            purchaseUnit.put("amount", amount);
            purchaseUnit.put("reference_id", pedidoId.toString());
            purchaseUnit.put("description", "Pedido #" + pedidoId);
            orderRequest.put("purchase_units", new Object[]{purchaseUnit});

            Map<String, Object> applicationContext = new HashMap<>();
            applicationContext.put("brand_name", "Jimenez Store");
            applicationContext.put("locale", "es-PE");
            applicationContext.put("shipping_preference", "NO_SHIPPING");
            applicationContext.put("user_action", "PAY_NOW");
            orderRequest.put("application_context", applicationContext);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(orderRequest, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    PAYPAL_API_BASE + "/v2/checkout/orders",
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Error al crear orden en PayPal"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Capturar orden de PayPal después del pago
     */
    @PostMapping("/capture-order")
    public ResponseEntity<?> captureOrder(@RequestBody Map<String, Object> request) {
        try {
            String orderId = (String) request.get("orderId");
            Integer pedidoId = (Integer) request.get("pedidoId");

            if (orderId == null || pedidoId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "orderId y pedidoId son requeridos"));
            }

            // Obtener token de acceso de PayPal
            String accessToken = getPayPalAccessToken();

            // Capturar la orden en PayPal
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    PAYPAL_API_BASE + "/v2/checkout/orders/" + orderId + "/capture",
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> captureData = response.getBody();

                // Buscar el pedido
                Pedido pedido = pedidoService.buscarPorId(pedidoId)
                        .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

                // Verificar si el pedido ya está pagado
                if (pedido.getEstado() == EstadoPedido.PAGADO) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("message", "El pedido ya estaba pagado");
                    result.put("pedidoId", pedido.getId());
                    return ResponseEntity.ok(result);
                }

                // Actualizar estado del pedido a PAGADO
                pedido.setEstado(EstadoPedido.PAGADO);
                pedidoService.actualizar(pedido);

                // Registrar el pago
                Pago pago = new Pago();
                pago.setPedido(pedido);
                // Usar Pago.MetodoPago en lugar de MetodoPago
                pago.setMetodo(Pago.MetodoPago.PAYPAL);  //
                pago.setMonto(pedido.getTotal());
                pago.setEstado(Pago.EstadoPago.APROBADO);
                pago.setReferenciaPasarela(orderId);
                pago.setFecha(LocalDateTime.now());
                pagoService.guardar(pago);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Pago procesado exitosamente");
                result.put("pedidoId", pedido.getId());
                result.put("captureId", captureData.get("id"));

                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Error al capturar el pago en PayPal"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Verificar estado de una orden en PayPal
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        try {
            String accessToken = getPayPalAccessToken();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    PAYPAL_API_BASE + "/v2/checkout/orders/" + orderId,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener token de acceso de PayPal
     */
    private String getPayPalAccessToken() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String auth = paypalClientId + ":" + paypalClientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    PAYPAL_API_BASE + "/v1/oauth2/token?grant_type=client_credentials",
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            } else {
                throw new RuntimeException("Error obteniendo token de PayPal");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo token de PayPal: " + e.getMessage());
        }
    }
}