package dto

import menu.Food

data class OrderDto(var menu: Food,
                    var amount: Int = 0)