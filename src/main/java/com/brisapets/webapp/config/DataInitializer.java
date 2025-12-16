package com.brisapets.webapp.config;

import com.brisapets.webapp.model.Role;
import com.brisapets.webapp.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("[INIT] Inicializando dados da aplicação...");
        
        // Criar roles se não existirem
        if (roleRepository.findByName("ROLE_CLIENTE").isEmpty()) {
            System.out.println("[INIT] Criando role ROLE_CLIENTE");
            roleRepository.save(new Role("ROLE_CLIENTE"));
        }
        
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            System.out.println("[INIT] Criando role ROLE_ADMIN");
            roleRepository.save(new Role("ROLE_ADMIN"));
        }
        
        System.out.println("[INIT] Inicialização concluída!");
    }
}