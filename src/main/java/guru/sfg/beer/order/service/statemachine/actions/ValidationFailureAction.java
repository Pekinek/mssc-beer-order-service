package guru.sfg.beer.order.service.statemachine.actions;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEvent;
import guru.sfg.beer.order.service.domain.BeerOrderStatus;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ValidationFailureAction implements Action<BeerOrderStatus, BeerOrderEvent> {

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> context) {
        BeerOrder beerOrder = context.getMessage().getHeaders().get(BeerOrderManagerImpl.BEER_ORDER, BeerOrder.class);
          log.error("Validation failed: " + beerOrder.getId());
    }
}
