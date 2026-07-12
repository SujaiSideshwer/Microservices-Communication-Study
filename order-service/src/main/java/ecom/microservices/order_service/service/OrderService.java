package ecom.microservices.order_service.service;

import ecom.microservices.order_service.client.InventoryClient;
import ecom.microservices.order_service.dto.Inventory;
import ecom.microservices.order_service.exception.Custom4xxException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderService {

    private final InventoryClient inventoryClient;

    private final RestTemplate restTemplate;

    private final RestClient restClient;

    public OrderService(InventoryClient inventoryClient, RestTemplate restTemplate, RestClient restClient) {
        this.inventoryClient = inventoryClient;
        this.restTemplate = restTemplate;
        this.restClient = restClient;
    }

    public String placeOrder(Long productId){
////Deprecated legacy RestTemplate used to communicate with Inventory:
//        String response = restTemplate.getForObject(
//                "http://localhost:8081/inventory/" + productId,
//                String.class
//        );

////Using RestClient - readable and manageable code
//        ResponseEntity<Inventory> entity= restClient.get()
//                .uri("http://localhost:8081/inventory/{productId}", productId)
//                .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
//                    throw new Custom4xxException(response.getStatusCode(), response.getHeaders());
//                })
//                .toEntity(Inventory.class);

////Using FeignClient - very easy,declarative - replaces REST calls with method calls
        Inventory inventory = inventoryClient.getInventory(productId);

//        updateInventory(entity.getBody());
        int quantity = inventory.getQuantity();
        updateInventory(inventory);

//        return entity.getBody() != null && entity.getBody().getQuantity() > 0
        return quantity > 0
                ? "order placed successfully" : "product out of stock";
    }

    private void updateInventory(Inventory inventory) {
        inventory.setQuantity(inventory.getQuantity() - 1);
//        restClient.post()
//                .uri("http://localhost:8081/inventory")
//                .body(inventory)
//                .retrieve()
//                .toBodilessEntity();
        inventoryClient.updateInventory(inventory);
    }
}
