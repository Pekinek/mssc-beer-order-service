package guru.sfg.beer.order.service.statemachine;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEvent;
import guru.sfg.beer.order.service.domain.BeerOrderStatus;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatus, BeerOrderEvent> {

    private final BeerOrderRepository beerOrderRepository;

    @Transactional
    @Override
    public void preStateChange(State<BeerOrderStatus, BeerOrderEvent> state,
                               Message<BeerOrderEvent> message,
                               Transition<BeerOrderStatus, BeerOrderEvent> transition,
                               StateMachine<BeerOrderStatus, BeerOrderEvent> stateMachine,
                               StateMachine<BeerOrderStatus, BeerOrderEvent> rootStateMachine) {
        log.debug("Pre-State Change");

        Optional.ofNullable(message)
                .flatMap(msg -> Optional.ofNullable((BeerOrder) msg.getHeaders().get(BeerOrderManagerImpl.BEER_ORDER)))
                .ifPresent(beerOrder -> {
                    log.debug("Saving state for order id: " + beerOrder + " Status: " + state.getId());

                    BeerOrder newBeerOrder = beerOrderRepository.getById(beerOrder.getId());
                    newBeerOrder.setOrderStatus(state.getId());
                    beerOrderRepository.saveAndFlush(newBeerOrder);
                });
    }
}
