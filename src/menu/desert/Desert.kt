package menu.desert

import menu.Food

open class Desert (name: String,
             description: String,
             price: Int,) : Food(name, description, price) {

    override fun displyInfo() {
        super.displyInfo()
        println("달콤한 디저트에요~")
    }
}