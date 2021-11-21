package guru.sfg.beer.order.service.services;

import com.mmocek.commons.model.ValidationResult;
import guru.sfg.beer.order.service.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeerOrderValidationResultListener {

    private BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESULT)
    public void listen(ValidationResult result) {

        log.debug(String.format("Validation result for order id: %s is %s", result.getOrderId(), result.isValid()));
        beerOrderManager.processValidationResult(result.getOrderId(), result.isValid());
    }
}
