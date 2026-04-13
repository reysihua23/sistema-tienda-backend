package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.Notificacion;
import com.reydi.tienda.repository.NotificacionRepository;
import com.reydi.tienda.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Override
    public List<Notificacion> listarTodos() {
        return notificacionRepository.findAll();
    }

    @Override
    public Optional<Notificacion> buscarPorId(Integer id) {
        return notificacionRepository.findById(id);
    }

    @Override
    public List<Notificacion> listarNoLeidas() {
        return notificacionRepository.findByLeidoFalse();
    }

    @Override
    public List<Notificacion> listarPorTipo(String tipo) {
        return notificacionRepository.findByTipo(tipo);
    }

    @Override
    public Notificacion guardar(Notificacion notificacion) {
        notificacion.setLeido(false);
        return notificacionRepository.save(notificacion);
    }

    @Override
    public Notificacion actualizar(Notificacion notificacion) {
        if (!notificacionRepository.existsById(notificacion.getId())) {
            throw new RuntimeException("Notificación no encontrada");
        }
        return notificacionRepository.save(notificacion);
    }

    @Override
    public void eliminar(Integer id) {
        if (!notificacionRepository.existsById(id)) {
            throw new RuntimeException("Notificación no encontrada");
        }
        notificacionRepository.deleteById(id);
    }

    @Override
    public void marcarComoLeida(Integer id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        notificacion.setLeido(true);
        notificacionRepository.save(notificacion);
    }

    @Override
    public void marcarTodasComoLeidas() {
        List<Notificacion> noLeidas = notificacionRepository.findByLeidoFalse();
        noLeidas.forEach(n -> n.setLeido(true));
        notificacionRepository.saveAll(noLeidas);
    }
}