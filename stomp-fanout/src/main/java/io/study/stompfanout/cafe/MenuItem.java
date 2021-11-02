package io.study.stompfanout.cafe;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MenuItem {
	private String itemName;
	private Integer price;

	@Builder
	public MenuItem(String itemName, Integer price){
		this.itemName = itemName;
		this.price = price;
	}
}
