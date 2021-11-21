package guru.sfg.beer.order.service.statemachine.actions;

import com.mmocek.commons.model.ValidateBeerOrderRequest;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEvent;
import guru.sfg.beer.order.service.domain.BeerOrderStatus;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidateOrderAction implements Action<BeerOrderStatus, BeerOrderEvent> {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> context) {
        BeerOrder beerOrder = context.getMessage().getHeaders().get("BeerOrder", BeerOrder.class);
        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER,
                new ValidateBeerOrderRequest(beerOrderMapper.beerOrderToDto(beerOrder)));
    }
}
