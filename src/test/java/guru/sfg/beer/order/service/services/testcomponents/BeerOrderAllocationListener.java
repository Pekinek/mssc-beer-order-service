package guru.sfg.beer.order.service.services.testcomponents;

import com.mmocek.commons.model.AllocateOrderRequest;
import com.mmocek.commons.model.AllocateOrderResult;
import guru.sfg.beer.order.service.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER)
    public void listen(Message<AllocateOrderRequest> msg) throws InterruptedException {
        AllocateOrderRequest request = msg.getPayload();
        boolean pendingInventory = false;
        boolean allocationError = false;
        boolean sendResponse = true;

        Thread.sleep(200);
        if (request.getBeerOrderDto().getCustomerRef() != null) {
            switch (request.getBeerOrderDto().getCustomerRef()) {
                case "fail-allocation":
                    allocationError = true;
                    break;
                case "partial-allocation":
                    pendingInventory = true;
                    break;
                case "dont-allocate":
                    sendResponse = false;
                    break;
            }
        }

        boolean finalPendingInventory = pendingInventory;

        request.getBeerOrderDto().getBeerOrderLines().forEach(beerOrderLineDto -> {
            if (finalPendingInventory) {
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() - 1);
            } else {
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
            }
        });

        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE,
                    AllocateOrderResult.builder()
                                       .beerOrderDto(request.getBeerOrderDto())
                                       .pendingInventory(pendingInventory)
                                       .allocationError(allocationError)
                                       .build());
        }
    }
}