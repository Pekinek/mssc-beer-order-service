package guru.sfg.beer.order.service.statemachine.actions;

import com.mmocek.commons.model.ValidateBeerOrderRequest;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEvent;
import guru.sfg.beer.order.service.domain.BeerOrderStatus;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateOrderAction implements Action<BeerOrderStatus, BeerOrderEvent> {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> context) {
        BeerOrder beerOrder = context.getMessage().getHeaders().get(BeerOrderManagerImpl.BEER_ORDER, BeerOrder.class);
        try {
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER, new ValidateBeerOrderRequest(beerOrderMapper.beerOrderToDto(beerOrder)));
        } catch (Exception e){
            log.error("Error during validation order: " + e);
        }
        log.debug("Validate request sent.");
    }
}
