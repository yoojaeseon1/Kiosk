import dto.OrderDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import menu.Food
import menu.coffee.Americano
import menu.coffee.Cafelatte
import menu.coffee.Cafemoca
import menu.coffee.Espresso
import menu.desert.CarrotCake
import menu.desert.ChocoCake
import menu.desert.Croiffle
import menu.desert.SaltBread
import menu.smoothy.BlueberrySmoothy
import menu.smoothy.LemonSmoothy
import menu.smoothy.StrawberrySmoothy
import menu.smoothy.YogurtSmoothy
import person.Client
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

class Kiosk private constructor() {

    private val basket = mutableListOf<OrderDto>()
    private val allMenus = mutableListOf<MutableList<Food>>()
    private val waitingOrders = mutableListOf<MutableList<OrderDto>>()
    private var totalPrice = 0
    private var client: Client? = null
    private var beforeCompleteTime: Long = 0L
    private var checkingStartTime = LocalDateTime.of(2024, Month.JUNE, 10, 13, 0,0)
    private var checkingEndTime = LocalDateTime.of(2024, Month.JUNE, 20, 19, 0,0)
    private var isContinueKiosk = true


    init {
        allMenus.add(mutableListOf(Americano(), Espresso(), Cafelatte(), Cafemoca()))
        allMenus.add(mutableListOf(BlueberrySmoothy(), LemonSmoothy(), StrawberrySmoothy(), YogurtSmoothy()))
        allMenus.add(mutableListOf(CarrotCake(), ChocoCake(), Croiffle(), SaltBread()))
        client = Client("yoo", 31, Gender.MALE, 100000, 10000)
    }

    companion object {
        @Volatile private var instance: Kiosk? = null
        fun getInstance() : Kiosk{
            if(instance == null) {
                synchronized(this) {
                    instance = Kiosk()
                }
            }
            return instance!!
        }
    }

    fun execute() {
        thread(start = true) {
            while(isContinueKiosk) {
                println("현재 대기중인 주문 수 : ${waitingOrders.size}")
                runBlocking {
                    launch {
                        delay(5000)
                    }
                }
            }
        }
        thread(start = true) {
            while (true) {
                val menuNumber = displayMainPage() // 타입 번호(1부터 시작)
                when(menuNumber) {
                    -1 -> {
                        isContinueKiosk = false
                        break
                    }
                    -2 -> continue
                    in 1..3 -> {
                        val menu = displayMenus(menuNumber) // 메뉴 번호(1부터 시작)
                        if(menu == -1)
                            continue
                        putOnMenuToBasket(allMenus[menuNumber-1][menu-1])
                        beforeCompleteTime = System.currentTimeMillis() / 1000
                    }
                    4 -> { // 장바구니 수정
                        val updateInfo = displayUpdateBasket()
                        if(updateInfo[0] >= 0)
                            updateOrder(updateInfo[0], updateInfo[1])
                        beforeCompleteTime = System.currentTimeMillis() / 1000
                    }
                    5 -> { // 주문 확정
                        if(displayCompleteOrder(basket))
                            completeOrder(basket)

                        beforeCompleteTime = System.currentTimeMillis() / 1000
                    }
                    6 -> { // 주문 취소
                        if(displayCancelBasket())
                            cancelOrder()
                        beforeCompleteTime = System.currentTimeMillis() / 1000
                    }
                    7-> { // 대기 중인 주문 결제하기
                        val selectedNumber = displayWaitingOrders()
                        if(selectedNumber > 0) {
                            completeOrder(waitingOrders[selectedNumber - 1])
                        }
                        beforeCompleteTime = System.currentTimeMillis() / 1000
                    }
                }
                continue
            }
        }
    }

    private fun calcTotalPrice(waitngOrders: MutableList<OrderDto>): Int{
        var totalPrice = 0
        for (orderMenu in waitngOrders) {
            totalPrice += orderMenu.menu.price * orderMenu.amount
        }
        return totalPrice
    }

    private fun updateOrder(menuIndex: Int, amount: Int) {
        val orderDto = basket[menuIndex]
        if(amount == 0) {
            basket.removeAt(menuIndex)
            totalPrice -= orderDto.menu.price * orderDto.amount
            return
        }

        orderDto.amount = amount
        totalPrice -= orderDto.menu.price * (orderDto.amount - amount)
    }

    private fun completeOrder(order: MutableList<OrderDto>): Boolean {

        val currentTime = LocalDateTime.now()

        if(currentTime in checkingStartTime.. checkingEndTime) {
            println("현재 시간은 ${convertTimeToHangle(currentTime)}입니다.")
            println("점검시간은 ${convertTimeToHangle(checkingStartTime)} ~ ${convertTimeToHangle(checkingEndTime)}이므로 결제할 수 없습니다.")

            if(order !in waitingOrders) {// basket이면
                waitingOrders.add(order.toMutableList())
                order.clear()
            }
            return false
        }

        val totalPrice = calcTotalPrice(order)
        if(client != null && client!!.balance < totalPrice) {
            println("잔액이 부족합니다.(현재 잔액 : ${client!!.balance})")
            if (order !in waitingOrders) {// basket이면
                waitingOrders.add(order.toMutableList())
                order.clear()
            }
            return false
        }

        client!!.balance -= totalPrice
        client!!.membershipPoint += (totalPrice * 0.05).toInt()
        order.clear()
        println("결제를 완료했습니다. (${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))})")
        println("현재 잔액 : ${client!!.balance}")
        return true
    }

    private fun cancelOrder() {
        basket.clear()
        totalPrice = 0
    }

    private fun displayMainPage(): Int{
        println("번호를 선택해주세요 : ")
        println("1. 커피")
        println("2. 스무디")
        println("3. 디저트")
        println("4. 장바구니 수정") // 장바구니 보여주기 + 수정
        println("5. 주문확정(현재주문)") // 장바구니 보여주기 + 주문 완료
        println("6. 주문 취소(현재주문)")
        println("7. 대기중인 주문")
        println("-1. 주문 종료")
        while (true) {
            try {
                val selectedNumber = readln().toInt()
                if(isWaitingTime()) {
                    println("다른 작업 완료 이후 3초 이내에 새로운 작업을 처리할 수 없습니다.")
                    return -2
                }
                if (selectedNumber in 1..7 || selectedNumber == -1)
                    return selectedNumber
                else
                    println("다시 입력해주세요 : ")
            } catch (e: NumberFormatException) {
                println("다시 입력해주세요 : ")
            }
        }
    }

    private fun displayMenus(menuNumber: Int): Int{
        var selectedNumber = menuNumber
        var isRepeatMenus = false
        while(true) {
            try {
                if(!isRepeatMenus) {
                    println("주문할 메뉴를 선택해주세요. : ")
                    println("메뉴명\t\t\t| 설명\t\t\t| 가격 ")
                    for ((index, menu) in allMenus[selectedNumber - 1].withIndex()) {
                        println("${index + 1}. ${menu.name}\t\t\t| ${menu.description}\t\t\t| ${menu.price}")
                    }
                    println("-1 : 이전메뉴")
                }
                selectedNumber = readln().toInt()
                if (selectedNumber in 1..4 || selectedNumber == -1)
                    return selectedNumber
                else
                    println(println("다시 입력해주세요. : "))
            } catch (e: NumberFormatException) {
                println("숫자를 입력해주세요. : ")
                isRepeatMenus = true
            } catch (e: IndexOutOfBoundsException) {
                println("다시 입력해주세요. : ")
                isRepeatMenus = true
            }
        }
    }

    private fun putOnMenuToBasket(menu: Food): Boolean {
        var isRepeatList = false
        while(true) {
            try {
                if (!isRepeatList) {
                    println("몇 개 구매하시겠습니까?")
                    println("메뉴명\t|개당 가격")
                    println("${menu.name}\t|${menu.price}")
                }
                val numBuying = readln().toInt()
                println("현재 메뉴 총 가격 : ${menu.price * numBuying}")
                println("장바구니에 추가하시겠습니까? (Y/N)")
                var onBasket = readln()
                while (onBasket !in setOf("Y", "N")) {
                    println("다시 입력해주세요(Y/N) : ")
                    onBasket = readln()
                }
                if(onBasket == "Y") {
                    basket.add(OrderDto(menu, numBuying))
                    return true
                } else
                    return false
            } catch (e: NumberFormatException) {
                println("숫자를 입력해주세요. : ")
                isRepeatList = true
            }
        }
    }
    
    // 장바구니 출력(수정용)
    // 리턴 값(배열)
    // [0] : 장바구니의 menu index
    // [1] : 수정된 수량
    private fun displayUpdateBasket(): IntArray {
        var isRepeatList = false
        while (true) {
            try {
                if (!isRepeatList) {
                    println("수정할 메뉴를 선택해주세요.(이전 메뉴 : -1) : ")
                    displayOrders(basket)
                }
                val selectedNumber = readln().toInt()
                when(selectedNumber) {
                    -1 -> return intArrayOf(-1,-1)
                    !in 1.. basket.size -> {
                        println("다시 입력해주세요.")
                        isRepeatList = true
                        continue
                    }
                }
                println("수정할 수량을 입력해주세요. : ")
                val numNewAmount = readln().toInt()
                return intArrayOf(selectedNumber-1, numNewAmount)
            } catch (e: NumberFormatException) {
                println("숫자를 입력해주세요. : ")
                isRepeatList = true
            }
        }
    }

    private fun displayCancelBasket() : Boolean{
        displayOrders(basket)
        println("주문을 취소하시겠습니까?(Y/N) : ")
        var isCanceled = readln()
        while (isCanceled !in setOf("Y", "N")) {
            println("다시 입력해주세요.(Y/N) : ")
            isCanceled = readln()
        }

        return when(isCanceled) {
            "Y" -> true
            else -> false
        }
    }

    // waitingOrder도 가능하게 인자 넣기
    private fun displayCompleteOrder(order: MutableList<OrderDto>): Boolean{
        displayOrders(order)
        println("주문을 확정하시겠습니까?(Y/N) : ")
        var isCompleted = readln()
        while (isCompleted !in setOf("Y", "N")) {
            println("다시 입력해주세요.(Y/N) : ")
            isCompleted = readln()
        }

        return when(isCompleted) {
            "Y" -> true
            else -> false
        }
    }

    private fun displayOrders(order: MutableList<OrderDto>){
        println("메뉴명\t| 수량\t | 합산 가격")
        for ((orderIndex, orderMenu) in order.withIndex()) {
            println("${orderIndex+1}. ${orderMenu.menu.name}\t| ${orderMenu.amount}\t| ${orderMenu.menu.price * orderMenu.amount}")
        }
        println("총 주문 금액 : ${calcTotalPrice(order)}")
    }

    private fun isWaitingTime(): Boolean {
        val currentTime = System.currentTimeMillis() / 1000
        return currentTime - beforeCompleteTime <= 3
    }
    
    private fun convertTimeToHangle(time: LocalDateTime): String{
        
        val convertToHangle = StringBuilder()
        
        when(time.hour) {
            in 0..12 -> {
                convertToHangle.append("오전")
                convertToHangle.append(time.hour)
            }
            else -> {
                convertToHangle.append("오후")
                convertToHangle.append(time.hour-12)
                
            }
        }
        convertToHangle.append("시 ")
        convertToHangle.append(time.minute)
        convertToHangle.append("분")
        
        return convertToHangle.toString()
    }

    private fun displayWaitingOrders(): Int{

        if(waitingOrders.size == 0) {
            println("대기중인 주문이 없습니다.")
            return 0
        } else {
            println("결제할 대기 주문을 선택해주세요. : ")
            for ((index, waitingOrder) in waitingOrders.withIndex()) {
                val totalPrice = calcTotalPrice(waitingOrder)
                println("${index + 1}. ${waitingOrder[0].menu.name} ${waitingOrder[0].amount}개 등 총 ${totalPrice}원")
            }
            println("-1. 이전 화면")

            var selectedNumber = 0
            while (true) {
                try {
                    selectedNumber = readln().toInt()
                    if (selectedNumber in 1..waitingOrders.size || selectedNumber == -1)
                        return selectedNumber
                    else
                        print("다시 입력해주세요 : ")
                } catch (e: NumberFormatException) {
                    print("다시 입력해주세요 : ")
                }
            }
        }
    }

}