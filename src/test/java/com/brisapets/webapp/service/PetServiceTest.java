package com.brisapets.webapp.service;

import com.brisapets.webapp.model.Pet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Inicia o contexto completo do Spring Boot para o teste
@SpringBootTest
// Garante que o teste será limpo do banco (H2) após a execução
@Transactional
// Pode ser útil para usar configurações específicas de teste se necessário
@ActiveProfiles("test")
public class PetServiceTest {

    // O objeto que queremos testar
    @Autowired
    private PetService petService;

    @Test
    void testSaveAndFindAll() {
        // 1. Arrange: Cria um novo Pet
        Pet novoPet = new Pet();
        novoPet.setNome("Pudim Teste");
        novoPet.setRaca("Caniche");
        novoPet.setIdade(3);
        novoPet.setCastrado(true);
        // Não esquecer do tutorId, senão pode falhar por not-null
        novoPet.setTutorId(99L);

        // 2. Act: Salva o Pet
        Pet petSalvo = petService.savePet(novoPet);

        // 3. Assert: Verifica se o Pet foi salvo
        assertNotNull(petSalvo.getId(), "O ID do Pet não deve ser nulo após salvar.");
        assertEquals("Pudim Teste", petSalvo.getNome(), "O nome do Pet deve corresponder.");

        // 4. Act & Assert: Busca todos os Pets
        List<Pet> pets = petService.findAllPets();
        assertFalse(pets.isEmpty(), "A lista de Pets não deve estar vazia.");
        assertTrue(pets.size() >= 1, "Deve haver pelo menos 1 Pet na lista.");
        
        // Verifica se o pet salvo está na lista
        boolean petEncontrado = pets.stream()
            .anyMatch(p -> "Pudim Teste".equals(p.getNome()) && "Caniche".equals(p.getRaca()));
        assertTrue(petEncontrado, "O pet salvo deve estar na lista.");
    }

}