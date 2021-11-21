package guru.sfg.beer.order.service.statemachine.actions;

import com.mmocek.commons.model.AllocateOrderRequest;
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


@Component
@RequiredArgsConstructor
@Slf4j
public class AllocateOrderAction implements Action<BeerOrderStatus, BeerOrderEvent> {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> context) {
        BeerOrder beerOrder = context.getMessage().getHeaders().get(BeerOrderManagerImpl.BEER_ORDER, BeerOrder.class);
        AllocateOrderRequest allocateOrderRequest = AllocateOrderRequest.builder()
                                                         .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder))
                                                         .build();

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER, allocateOrderRequest);
        log.debug("Sent allocation request for order id: " + beerOrder.getId());
    }
}
