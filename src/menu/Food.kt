package menu

open class Food (var name: String,
                 var description: String,
                 var price: Int,) {

    open fun displyInfo(){
        println("이름 = ${name}, 가격 = ${price}, 설명 = ${description}")
    }
}