package com.domicilio.domijose.services;

import com.domicilio.domijose.dto.MetodoPagoDTO;
import com.domicilio.domijose.mappers.MetodoPagoMapper;
import com.domicilio.domijose.models.MetodoPago;
import com.domicilio.domijose.repositories.MetodoPagoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetodoPagoService {
    private static final Logger log = LoggerFactory.getLogger(MetodoPagoService.class);

    private final MetodoPagoRepository repository;
    private final MetodoPagoMapper mapper;
    private final FileService fileService;

    public MetodoPagoService(MetodoPagoRepository repository, MetodoPagoMapper mapper, FileService fileService) {
        this.repository = repository;
        this.mapper = mapper;
        this.fileService = fileService;
    }

    public List<MetodoPagoDTO> getMetodosActivos() {
        log.debug("Obteniendo métodos de pago activos");
        List<MetodoPago> entities = repository.findByActivoTrue();
        return entities.stream().map(mapper::toDto).toList();
    }

    public List<MetodoPagoDTO> getCuentas() {
        log.debug("Obteniendo cuentas bancarias activas");
        return repository.findByActivoTrue().stream()
                .filter(m -> m.getNumeroCuenta() != null && !m.getNumeroCuenta().isEmpty())
                .map(mapper::toDto)
                .toList();
    }

    public List<MetodoPagoDTO> getQrs() {
        log.debug("Obteniendo QRs activos");
        return repository.findByActivoTrue().stream()
                .filter(m -> m.getQrUrl() != null && !m.getQrUrl().isEmpty())
                .map(mapper::toDto)
                .toList();
    }

    public MetodoPagoDTO save(MetodoPagoDTO dto) {
        log.info("Guardando método de pago: {}", dto.getBanco());

        String qrUrl = null;
        if (dto.getQrFile() != null && !dto.getQrFile().isEmpty()) {
            qrUrl = fileService.saveImage(dto.getQrFile());
            log.info("QR guardado: {}", qrUrl);
        }

        MetodoPago entity = mapper.toEntity(dto);
        entity.setQrUrl(qrUrl);
        entity.setActivo(true);

        repository.save(entity);
        return mapper.toDto(entity);
    }

    public void delete(Long id) {
        log.info("Eliminando método de pago: {}", id);
        repository.deleteById(id);
    }

    public MetodoPagoDTO saveCuenta(MetodoPagoDTO dto) {
        log.info("Guardando cuenta: {}", dto.getBanco());
        MetodoPago entity = mapper.toEntity(dto);
        entity.setQrUrl(null);
        entity.setActivo(true);
        repository.save(entity);
        return mapper.toDto(entity);
    }

    public MetodoPagoDTO saveQr(MetodoPagoDTO dto) {
        log.info("Guardando QR");
        String qrUrl = null;
        if (dto.getQrFile() != null && !dto.getQrFile().isEmpty()) {
            qrUrl = fileService.saveImage(dto.getQrFile(), true);
            log.info("QR guardado: {}", qrUrl);
        }
        MetodoPago entity = mapper.toEntity(dto);
        entity.setQrUrl(qrUrl);
        entity.setBanco(null);
        entity.setTipoCuenta(null);
        entity.setNumeroCuenta(null);
        entity.setNombreTitular(null);
        entity.setActivo(true);
        repository.save(entity);
        return mapper.toDto(entity);
    }
}