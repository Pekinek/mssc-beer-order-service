package guru.sfg.beer.order.service.services;

import com.mmocek.commons.model.BeerOrderDto;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEvent;
import guru.sfg.beer.order.service.domain.BeerOrderStatus;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.statemachine.BeerOrderStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String BEER_ORDER = "BEER_ORDER";

    private final StateMachineFactory<BeerOrderStatus, BeerOrderEvent> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor changeInterceptor;


    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatus.NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEvent.VALIDATE_ORDER);
        return savedBeerOrder;
    }


    @Override
    @Transactional
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {

        BeerOrder beerOrder = beerOrderRepository.findOneById(beerOrderId);
        if (isValid) {
            sendBeerOrderEvent(beerOrder, BeerOrderEvent.VALIDATION_PASSED);
            BeerOrder validatedOrder = beerOrderRepository.findOneById(beerOrderId);
            sendBeerOrderEvent(validatedOrder, BeerOrderEvent.ALLOCATE_ORDER);
        } else {
            sendBeerOrderEvent(beerOrder, BeerOrderEvent.VALIDATION_FAILED);
        }
    }

    @Override
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEvent.ALLOCATION_SUCCESS);
            updateAllocatedQty(beerOrderDto);
        }, () -> log.error("Order Id Not Found: " + beerOrderDto.getId()));
    }

    @Override
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEvent.ALLOCATION_NO_INVENTORY);
            updateAllocatedQty(beerOrderDto);
        }, () -> log.error("Order Id Not Found: " + beerOrderDto.getId()));

    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        allocatedOrderOptional.ifPresentOrElse(allocatedOrder -> {
            allocatedOrder.getBeerOrderLines()
                          .forEach(beerOrderLine -> beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                              if (beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
                                  beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
                              }
                          }));

            beerOrderRepository.saveAndFlush(allocatedOrder);
        }, () -> log.error("Order Not Found. Id: " + beerOrderDto.getId()));
    }

    @Override
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        beerOrderOptional.ifPresentOrElse(beerOrder -> sendBeerOrderEvent(beerOrder, BeerOrderEvent.ALLOCATION_FAILED),
                () -> log.error("Order Not Found. Id: " + beerOrderDto.getId()));

    }

    @Override
    public void beerOrderPickedUp(UUID id) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(id);

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            //do process
            sendBeerOrderEvent(beerOrder, BeerOrderEvent.BEER_ORDER_PICKED_UP);
        }, () -> log.error("Order Not Found. Id: " + id));
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEvent eventEnum) {
        StateMachine<BeerOrderStatus, BeerOrderEvent> sm = build(beerOrder);
        Message<BeerOrderEvent> msg = MessageBuilder.withPayload(eventEnum).setHeader(BEER_ORDER, beerOrder).build();
        sm.sendEvent(msg);
    }

    private StateMachine<BeerOrderStatus, BeerOrderEvent> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatus, BeerOrderEvent> sm = stateMachineFactory.getStateMachine(beerOrder.getId());
        sm.stopReactively().block();
        StateMachineContext<BeerOrderStatus, BeerOrderEvent> context = new DefaultStateMachineContext<>(
                beerOrder.getOrderStatus(), null, null, null);
        sm.getStateMachineAccessor().doWithAllRegions(function -> {
            function.resetStateMachineReactively(context).block();
            function.addStateMachineInterceptor(changeInterceptor);
        });
        sm.startReactively().block();
        return sm;
    }
}