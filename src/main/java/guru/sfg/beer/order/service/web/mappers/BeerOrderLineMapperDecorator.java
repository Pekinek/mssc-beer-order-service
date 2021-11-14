package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.services.BeerService;
import guru.sfg.beer.order.service.web.model.BeerDto;
import guru.sfg.beer.order.service.web.model.BeerOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public abstract class BeerOrderLineMapperDecorator implements BeerOrderLineMapper{
    private BeerService beerService;
    private BeerOrderLineMapper mapper;

    @Autowired
    public void setBeerService(BeerService beerService) {
        this.beerService = beerService;
    }

    @Autowired
    public void setMapper(BeerOrderLineMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        BeerOrderLineDto dto = mapper.beerOrderLineToDto(line);

        Optional<BeerDto> beerDtoOptional = beerService.getBeerDto(line.getUpc());

        beerDtoOptional.ifPresent(beerDto -> {
            dto.setBeerName(beerDto.getBeerName());
            dto.setBeerStyle(beerDto.getBeerStyle());
            dto.setPrice(beerDto.getPrice());
            dto.setBeerId(beerDto.getId());
        });

        return dto;
    }
}
