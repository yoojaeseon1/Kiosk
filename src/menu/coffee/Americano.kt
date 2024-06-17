package menu.coffee

import menu.Food

class Americano (name: String = "아메리카노",
                description: String = "아메리카노 입니다.",
                price: Int = 3000) : Coffee(name, description, price) {
}