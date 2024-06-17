package menu.coffee

import menu.Food

open class Coffee(name: String,
                  description: String,
                  price: Int,
                  var isHot: Boolean = true) : Food(name, description, price){

    override fun displyInfo() {
        super.displyInfo()
        when(isHot) {
            true -> println("뜨겁습니다.")
            else -> println("차갑습니다.")
        }
    }
}