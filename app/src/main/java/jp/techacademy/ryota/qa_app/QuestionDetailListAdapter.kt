package jp.techacademy.ryota.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class QuestionDetailListAdapter(context: Context, private val mQuestion: Question) : BaseAdapter() {
    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater? = null

    init {
        mLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + mQuestion.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return mQuestion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView =
                    mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
            }
            val body = mQuestion.body
            val name = mQuestion.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name

            val bytes = mQuestion.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    .copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }

            setFavButtonVisibility(convertView)
        } else {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val answer = mQuestion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name
        }

        return convertView
    }

    private fun setFavButtonVisibility(convertView: View) {
        val user = FirebaseAuth.getInstance().currentUser
        val favButton = convertView.findViewById<View>(R.id.favButton) as ImageButton

        if (user == null) {
            favButton.visibility = View.GONE
        } else {
            favButton.visibility = View.VISIBLE

            val favoriteRef =
                FirebaseDatabase.getInstance().reference.child(FavoritesPATH).child(user.uid)

            favoriteRef.child(mQuestion.questionUid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val favoriteState = dataSnapshot.child("is_favorite").value
                    val data = HashMap<String, Boolean>()

                    when (favoriteState) {
                        null, false -> {
                            favButton.setImageResource(R.drawable.not_favorite)
                            data["is_favorite"] = true
                        }

                        true -> {
                            favButton.setImageResource(R.drawable.favorite)
                            data["is_favorite"] = false
                        }
                    }

                    favButton.setOnClickListener {
                        favoriteRef.child(mQuestion.questionUid).setValue(data)
                    }
                }

                override fun onCancelled(dataSnapshot: DatabaseError) {
                }
            })
        }
    }
}
