package com.app.dto;



import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class OrderRequest {
private List<OrderLineItemsDto> orderLineItemDtoList;
}
