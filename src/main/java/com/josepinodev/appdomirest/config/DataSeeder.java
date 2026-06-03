package com.josepinodev.appdomirest.config;

import com.josepinodev.appdomirest.model.ECategoria;
import com.josepinodev.appdomirest.model.ERole;
import com.josepinodev.appdomirest.model.ProductoEntity;
import com.josepinodev.appdomirest.model.RoleEntity;
import com.josepinodev.appdomirest.model.RutaDomicilioEntity;
import com.josepinodev.appdomirest.model.UserEntity;
import com.josepinodev.appdomirest.repository.ProductoRepository;
import com.josepinodev.appdomirest.repository.RoleRepository;
import com.josepinodev.appdomirest.repository.RutaDomicilioRepository;
import com.josepinodev.appdomirest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

        private final RoleRepository roleRepository;
        private final UserRepository userRepository;
        private final ProductoRepository productoRepository;
        private final RutaDomicilioRepository rutaRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        @Transactional
        public void run(String... args) {
                if (roleRepository.count() == 0) {
                        log.info("Sembrando roles...");
                        roleRepository.saveAll(List.of(
                                        new RoleEntity(null, ERole.ADMIN),
                                        new RoleEntity(null, ERole.USER),
                                        new RoleEntity(null, ERole.DEV)));
                }

                if (productoRepository.count() > 0) {
                        log.info("Datos ya existentes, se omite el seed.");
                        return;
                }

                RoleEntity roleAdmin = roleRepository.findByName(ERole.ADMIN).orElse(null);
                RoleEntity roleDev = roleRepository.findByName(ERole.DEV).orElse(null);

                log.info("Sembrando usuario admin...");
                UserEntity admin = new UserEntity();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setTelefono("3000000000");
                admin.setEmail("admin@appdomirest.com");
                admin.setRoles(Set.of(roleAdmin, roleDev));
                admin.setActivo(true);
                admin = userRepository.save(admin);

                log.info("Sembrando rutas de domicilio...");
                List<RutaDomicilioEntity> rutas = List.of(
                                ruta("Barrio Gaitán", 5000),
                                ruta("Barrio El Llano", 6000),
                                ruta("Barrio San Fernando", 7000),
                                ruta("Barrio La Estrella", 8000),
                                ruta("Barrio La Pradera", 9000),
                                ruta("Barrio Villa del Rosario", 10000),
                                ruta("Barrio El Centro", 11000),
                                ruta("Barrio Las Américas", 12000),
                                ruta("Barrio La Floresta", 13000),
                                ruta("Barrio Santa María", 15000));
                rutaRepository.saveAll(rutas);

                log.info("Sembrando productos...");
                List<ProductoEntity> productos = List.of(
                                producto("Perro Caliente",
                                                "Pan artesanal, salchicha americana, papas en hilo, queso cheddar, salsas de la casa",
                                                14000, "PROD-0001", "imagen1.png", false, null,
                                                ECategoria.COMIDA_RAPIDA, admin),
                                producto("Hamburguesa",
                                                "Carne angus 200g, doble queso, lechuga, tomate, cebolla caramelizada, papas",
                                                18000, "PROD-0002", "imagen2.png", false, null,
                                                ECategoria.COMIDA_RAPIDA, admin),
                                producto("Hamburguesa Borbón",
                                                "Carne angus 250g, bourbon glaze, queso ahumado, cebolla crispy, tocineta",
                                                22000, "PROD-0003", "imagen3.png", false, null,
                                                ECategoria.COMIDA_RAPIDA, admin),
                                producto("Carne Asado",
                                                "Lomo de res 300g asado a la parrilla, chimichurri, papas y ensalada",
                                                30000, "PROD-0004", "imagen4.png", false, null,
                                                ECategoria.CORTES_DE_CARNE, admin),
                                producto("Carne Tomahawk",
                                                "Tomahawk 600g sellado al carbón, mantequilla de hierbas, verduras asadas",
                                                55000, "PROD-0005", "imagen5.png", true, 48000,
                                                ECategoria.CORTES_DE_CARNE, admin),
                                producto("Ensalada César con Pollo",
                                                "Lechuga romana, pollo grillé, crutones, parmesano, aderezo césar",
                                                16000, "PROD-0006", "imagen6.png", false, null, ECategoria.ENSALADAS,
                                                admin),
                                producto("Pollo Asado",
                                                "Mitad de pollo marinado, asado al carbón, papas y ensalada",
                                                20000, "PROD-0007", "imagen7.png", false, null, ECategoria.POLLO,
                                                admin),
                                producto("Pollo Broaster",
                                                "Presas de pollo empanizadas, fritas, con papas y salsa de la casa",
                                                17000, "PROD-0008", "imagen8.png", false, null, ECategoria.POLLO,
                                                admin),
                                producto("Papas Fritas",
                                                "Papas frescas cortadas a mano, fritas, sal marina, acompañantes",
                                                8000, "PROD-0009", "imagen9.png", true, 6000, ECategoria.ENTRADAS,
                                                admin),
                                producto("Yuca Frita",
                                                "Yuca fresca cortada en bastones, frita, servida con hogao y suero",
                                                9000, "PROD-0010", "imagen10.png", false, null, ECategoria.ENTRADAS,
                                                admin),
                                producto("Hayaquitas",
                                                "Hayacas venezolanas pequeñas, rellenas de cerdo, pollo y garbanzos",
                                                12000, "PROD-0011", "imagen11.png", false, null, ECategoria.ENTRADAS,
                                                admin),
                                producto("Pepsi 2.5 Litros",
                                                "Refresco de cola, 2.5 litros", 
                                                5500, "PROD-0012", "imagen12.png", false, null, ECategoria.BEBIDA,
                                                admin));
                productoRepository.saveAll(productos);

                log.info("Seed completado: admin, {} rutas, {} productos", rutas.size(), productos.size());
        }

        private RutaDomicilioEntity ruta(String barrio, int precio) {
                RutaDomicilioEntity r = new RutaDomicilioEntity();
                r.setBarrio(barrio);
                r.setPrecio(BigDecimal.valueOf(precio));
                r.setActivo(true);
                return r;
        }

        private ProductoEntity producto(String nombre, String descripcion, int precioVenta,
                        String codigo, String imagen, boolean enPromocion,
                        Integer precioPromocional, ECategoria categoria, UserEntity creador) {
                ProductoEntity p = new ProductoEntity();
                p.setNombre(nombre);
                p.setDescripcion(descripcion);
                p.setPrecioCompra(BigDecimal.valueOf(precioVenta * 0.6));
                p.setPrecioVenta(BigDecimal.valueOf(precioVenta));
                p.setCodigo(codigo);
                p.setImagenUrl("/imagenes/productos/" + imagen);
                p.setEnPromocion(enPromocion);
                if (enPromocion && precioPromocional != null) {
                        p.setPrecioPromocional(BigDecimal.valueOf(precioPromocional));
                }
                p.setCategoria(categoria);
                p.setActivo(true);
                p.setDisponible(true);
                p.setUsuarioCreador(creador);
                return p;
        }
}
