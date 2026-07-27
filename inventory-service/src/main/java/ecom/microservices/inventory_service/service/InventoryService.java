package ecom.microservices.inventory_service.service;

import ecom.microservices.inventory_service.model.Inventory;
import ecom.microservices.inventory_service.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository repository;

    public Inventory checkStock(Long productId) {
        Optional<Inventory> inv = repository.findById(productId);
        return inv.get();
    }

    /**
     * Called from the Kafka listener when a payment completes. Decrements stock for the
     * ordered quantity and persists it. Returns a human-readable status that we then
     * stream out on the "order-placed" topic.
     */
    @Transactional
    public String reserveStock(Long productId, int quantity) {
        Optional<Inventory> found = repository.findById(productId);
        if (found.isEmpty()) {
            return "FAILED: product " + productId + " not found";
        }
        Inventory inventory = found.get();
        if (inventory.getQuantity() < quantity) {
            return "FAILED: insufficient stock for product " + productId;
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
        repository.save(inventory);
        return "PLACED: reserved " + quantity + " unit(s) of product " + productId
                + " (remaining stock: " + inventory.getQuantity() + ")";
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
