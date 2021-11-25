package guru.sfg.beer.order.service.statemachine.actions;

import guru.sfg.beer.order.service.domain.BeerOrderEvent;
import guru.sfg.beer.order.service.domain.BeerOrderStatus;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

public class CancelAllocatedOrderAction implements Action<BeerOrderStatus, BeerOrderEvent> {

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> context) {

    }
}
