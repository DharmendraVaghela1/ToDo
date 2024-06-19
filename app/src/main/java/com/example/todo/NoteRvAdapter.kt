package com.example.todo

import android.content.Context
import android.content.SharedPreferences
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteRVAdapter(
    val context: Context,
    val noteClickDeleteInterface: NoteClickDeleteInterface,
    val noteClickInterface: NoteClickInterface
) :
    RecyclerView.Adapter<NoteRVAdapter.ViewHolder>() {

    // on below line we are creating a
    // variable for our all notes list.
    private val allNotes = ArrayList<Note>()
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    // on below line we are creating a view holder class.
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // on below line we are creating an initializing all our
        // variables which we have added in layout file.
        val noteTV = itemView.findViewById<TextView>(R.id.idTVNote)
        val dateTV = itemView.findViewById<TextView>(R.id.idTVDate)
        val noteCheckBox: CheckBox = itemView.findViewById(R.id.noteCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflating our layout file for each item of recycler view.
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.note_rv_item,
            parent, false
        )
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentNote = allNotes[position]

        // Retrieve the stored checkbox state from SharedPreferences
        val isChecked = sharedPreferences.getBoolean("checkbox_state_${currentNote.id}", false)

        // Set the checkbox state only for new tasks, leave existing tasks unchanged
        if (isChecked) {
            holder.noteCheckBox.isChecked = true
            val spannableString = SpannableString(currentNote.noteTitle)
            spannableString.setSpan(StrikethroughSpan(), 0, spannableString.length, 0)
            holder.noteTV.text = spannableString
        } else {
            holder.noteCheckBox.isChecked = false
            holder.noteTV.text = currentNote.noteTitle
        }

        // Save checkbox state when it changes
        holder.noteCheckBox.setOnCheckedChangeListener(null) // Avoid listener trigger during initialization
        holder.noteCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // Save checkbox state to SharedPreferences
            editor.putBoolean("checkbox_state_${currentNote.id}", isChecked)
            editor.apply()

            // Apply or remove strikethrough effect based on checkbox state
            if (isChecked) {
                val spannableString = SpannableString(currentNote.noteTitle)
                spannableString.setSpan(StrikethroughSpan(), 0, spannableString.length, 0)
                holder.noteTV.text = spannableString
            } else {
                holder.noteTV.text = currentNote.noteTitle
            }
        }

        // Set other views
        holder.dateTV.text = "Last Updated: " + currentNote.timeStamp

        // Set click listener to handle item click
        holder.itemView.setOnClickListener {
            noteClickInterface.onNoteClick(currentNote)
        }
    }

    override fun getItemCount(): Int {
        // on below line we are
        // returning our list size.
        return allNotes.size
    }

    // below method is use to update our list of notes.
    fun updateList(newList: List<Note>) {
        // on below line we are clearing
        // our notes array list
        allNotes.clear()
        // on below line we are adding a
        // new list to our all notes list.
        allNotes.addAll(newList)
        // on below line we are calling notify data
        // change method to notify our adapter.
        notifyDataSetChanged()
    }
    fun removeItem(position: Int) {
        val note = allNotes[position]
        allNotes.removeAt(position)
        notifyItemRemoved(position)
        // Delete note from Room database via interface
        noteClickDeleteInterface.onDeleteIconClick(note)
    }
}

interface NoteClickDeleteInterface {
    // creating a method for click
    // action on delete image view.
    fun onDeleteIconClick(note: Note)
}

interface NoteClickInterface {
    // creating a method for click action
    // on recycler view item for updating it.
    fun onNoteClick(note: Note)
}