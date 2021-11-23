package guru.sfg.beer.order.service.services.testcomponents;

import com.mmocek.commons.model.ValidateBeerOrderRequest;
import com.mmocek.commons.model.ValidationResult;
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
public class BeerOrderValidationListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER)
    public void list(Message<ValidateBeerOrderRequest> msg) throws InterruptedException {
        boolean isValid = true;
        boolean sendResponse = true;

        Thread.sleep(200);
        ValidateBeerOrderRequest request = msg.getPayload();

        //condition to fail validation
        if (request.getBeerOrderDto().getCustomerRef() != null) {
            if (request.getBeerOrderDto().getCustomerRef().equals("fail-validation")){
                isValid = false;
            } else if (request.getBeerOrderDto().getCustomerRef().equals("dont-validate")){
                sendResponse = false;
            }
        }

        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESULT,
                    ValidationResult.builder()
                                       .isValid(isValid)
                                       .orderId(request.getBeerOrderDto().getId())
                                       .build());
        }
    }
}