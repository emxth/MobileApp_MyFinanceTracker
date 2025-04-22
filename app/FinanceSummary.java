data class FinanceSummary(
        val income: Double,
        val expense: Double,
        val balance: Double
)

fun getFinanceSummary(context: Context): FinanceSummary {
    val sharedPreferences = context.getSharedPreferences("transactions", Context.MODE_PRIVATE)
    val json = sharedPreferences.getString("transaction_list", null)

    if (!json.isNullOrEmpty()) {
        val gson = Gson()
        val type = object : TypeToken<List<Transaction>>() {}.type
        val transactions: List<Transaction> = gson.fromJson(json, type)

        val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        val balance = income - expense

        return FinanceSummary(income, expense, balance)
    }

    return FinanceSummary(0.0, 0.0, 0.0)
}
