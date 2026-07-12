package ecom.microservices.inventory_service.service;

import ecom.microservices.inventory_service.model.Inventory;
import ecom.microservices.inventory_service.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository repository;

    public Inventory checkStock(Long productId) {
        Optional<Inventory> inv = repository.findById(productId);
        return inv.get();
    }

    public String addProduct(Inventory inventory) {
        repository.save(inventory);
        return "product added";
    }

    public String updateProduct(Inventory inventory) {
        repository.save(inventory);
        return "product updated";
    }

    public String deleteProduct(Long productId) {
        repository.deleteById(productId);
        return productId + " was deleted";
    }
}
