package com.example.sentimentanalysis

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var etInput: EditText
    private lateinit var btnSubmit: Button
    private lateinit var resultLabel: TextView
    private lateinit var emojiLabel: TextView
    private lateinit var rootLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        rootLayout = findViewById(R.id.rootLayout)
        etInput = findViewById(R.id.etInput)
        btnSubmit = findViewById(R.id.btnSubmit)
        resultLabel = findViewById(R.id.resultLabel)
        emojiLabel = findViewById(R.id.tvEmoji)

        // Retrofit setup
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.102:5000") // Change IP accordingly
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(SentimentApi::class.java)

        // Button click event
        btnSubmit.setOnClickListener {
            val input = etInput.text.toString().trim()

            if (input.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập văn bản", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Send request
            api.analyzeSentiment(RequestBody(input)).enqueue(object : Callback<SentimentResponse> {
                override fun onResponse(
                    call: Call<SentimentResponse>,
                    response: Response<SentimentResponse>
                ) {
                    if (response.isSuccessful) {
                        val sentiment = response.body()
                        resultLabel.text = "Kết quả: ${sentiment?.label} (${sentiment?.confidence})"

                        // Set emoji and background based on label
                        when (sentiment?.label) {
                            "positive" -> {
                                emojiLabel.text = "😊"
                                rootLayout.setBackgroundColor(Color.parseColor("#00C853")) // Green
                            }
                            "neutral" -> {
                                emojiLabel.text = "😐"
                                rootLayout.setBackgroundColor(Color.parseColor("#FFD54F")) // Yellow-ish
                            }
                            "negative" -> {
                                emojiLabel.text = "😞"
                                rootLayout.setBackgroundColor(Color.parseColor("#D32F2F")) // Red
                            }
                            else -> {
                                emojiLabel.text = "🤔"
                                rootLayout.setBackgroundColor(Color.GRAY)
                            }
                        }

                    } else {
                        resultLabel.text = "Lỗi: Không thể phân tích"
                        emojiLabel.text = "🤔"
                        rootLayout.setBackgroundColor(Color.GRAY)
                    }
                }

                override fun onFailure(call: Call<SentimentResponse>, t: Throwable) {
                    resultLabel.text = "Lỗi kết nối: ${t.message}"
                    emojiLabel.text = "😞"
                    rootLayout.setBackgroundColor(Color.DKGRAY)
                }
            })
        }
    }
}
