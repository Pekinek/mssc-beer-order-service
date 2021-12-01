package guru.sfg.beer.order.service.web.mappers;

import com.mmocek.commons.model.CustomerDto;
import guru.sfg.beer.order.service.domain.Customer;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
public interface CustomerMapper {

    CustomerDto customerToCustomerDto(Customer customer);

    Customer customerDtoToCustomer(CustomerDto customerDto);
}
