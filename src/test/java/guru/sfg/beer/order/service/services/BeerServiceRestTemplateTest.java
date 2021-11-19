package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.bootstrap.BeerOrderBootStrap;
import com.mmocek.commons.model.BeerDto;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
class BeerServiceRestTemplateTest {

    @Autowired
    BeerServiceRestTemplate beerServiceRestTemplate;

    @Disabled("only for manual testing")
    @Test
    void getBeerDto() {
        Optional<BeerDto> beerDto = beerServiceRestTemplate.getBeerDto(BeerOrderBootStrap.BEER_1_UPC);
        beerDto.ifPresent(System.out::println);

    }
}