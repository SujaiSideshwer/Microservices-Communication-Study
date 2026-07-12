package ecom.microservices.order_service.service;

import ecom.microservices.order_service.dto.Inventory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderService {

    private final RestTemplate restTemplate;

    public OrderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String placeOrder(Long productId){
        ResponseEntity<Inventory> entity = restTemplate.getForEntity(
                "http://localhost:8081/inventory/" + productId,
                Inventory.class
        );

        Inventory inventory = entity.getBody();

        updateInventory(inventory);

        return entity.getBody() != null && entity.getBody().getQuantity() > 0
                ? "order placed successfully" : "product out of stock";
    }

    private void updateInventory(Inventory inventory) {
        inventory.setQuantity(inventory.getQuantity() - 1);
        restTemplate.postForEntity("http://localhost:8081/inventory", inventory, String.class);
    }
}
