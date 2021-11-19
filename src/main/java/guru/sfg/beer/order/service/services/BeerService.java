package guru.sfg.beer.order.service.services;

import com.mmocek.commons.model.BeerDto;

import java.util.Optional;

public interface BeerService {

    Optional<BeerDto> getBeerDto(String beerUpc);
}
