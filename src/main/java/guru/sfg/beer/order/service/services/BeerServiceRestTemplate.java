package guru.sfg.beer.order.service.services;

import com.mmocek.commons.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@ConfigurationProperties(prefix = "sfg.brewery", ignoreUnknownFields = false)
@Component
public class BeerServiceRestTemplate implements BeerService {

    public final static String BEER_PATH = "/api/v1/beerUpc/";
    private final RestTemplate restTemplate;
    private String beerServiceHost;

    public BeerServiceRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void setBeerServiceHost(String beerServiceHost) {
        this.beerServiceHost = beerServiceHost;
    }

    @Override
    public Optional<BeerDto> getBeerDto(String beerUpc) {

        log.debug("Calling Beer Service");

        BeerDto beerDto = restTemplate.getForObject(beerServiceHost + BEER_PATH+beerUpc, BeerDto.class);

        return Optional.ofNullable(beerDto);
    }
}
