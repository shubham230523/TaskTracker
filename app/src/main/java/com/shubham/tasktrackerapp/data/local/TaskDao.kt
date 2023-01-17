package com.shubham.tasktrackerapp.data.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Dao
interface TaskDao {

    @Query("Select * from tbTask")
    fun getAllTasks(): LiveData<List<Task>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tbTask")
    suspend fun deleteTable()

    // Missed task table
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskIntoMissedTable(task: MissedTask)

    /**
     * For getting the last week missed tasks
     *
     * for ref - SELECT SUM(Value) FROM YourTable WHERE Value = 0 AND Date < (SELECT MIN(Date) FROM YourTable WHERE Value = 1)
     * https://stackoverflow.com/questions/40607939/sql-using-while-loop-until-nex-row-doesnt-match-condition
     *
     * SELECT * FROM tbTasksMissed WHERE date(due_date) BETWEEN date(:from) AND date(:to)" +
    "AND date(due_date) > (SELECT MIN(date(due_date)) FROM tbTasksMissed WHERE date(due_date) < date(:from)")

     */
    @Query("SELECT * FROM tbTasksMissed WHERE JULIANDAY(:date)-JULIANDAY(due_date) < 8 " +
            "AND JULIANDAY(due_date) - JULIANDAY((SELECT MAX(due_date) from tbTasksMissed WHERE JULIANDAY(:date)-JULIANDAY(due_date) >= 8)) > 0")
    fun getLastWeekMissedTasks(date : String): LiveData<List<MissedTask>>

    /**
     * For getting the last month missed tasks
     */
    @Query("SELECT * FROM tbTasksMissed WHERE JULIANDAY(:date)-JULIANDAY(due_date) < 31 " +
            "AND JULIANDAY(due_date) - JULIANDAY((SELECT MAX(due_date) from tbTasksMissed WHERE JULIANDAY(:date)-JULIANDAY(due_date) >= 31)) > 0")
    fun getLastMonthMissedTasks(date: String): LiveData<List<MissedTask>>

    /**
     * For deleting the missed tasks older than one month
     */
    @Query("DELETE FROM tbTasksMissed WHERE JULIANDAY(:currentDate) - JULIANDAY(due_date) > 30")
    suspend fun deleteMissedTasksOlderThanOneMonth(currentDate: String)

    /**
     * For getting the last week done tasks
     */
    @Query("SELECT * FROM tbTasksDone WHERE JULIANDAY(:date)-JULIANDAY(due_date) < 8 " +
            "AND JULIANDAY(due_date) - JULIANDAY((SELECT MAX(due_date) from tbTasksDone WHERE JULIANDAY(:date)-JULIANDAY(due_date) >= 8)) > 0")
    fun getLastWeekDoneTasks(date: String): LiveData<List<TaskDone>>

    /**
     * For getting the last month done tasks
     */
    @Query("SELECT * FROM tbTasksDone WHERE JULIANDAY(:date)-JULIANDAY(due_date) < 31 " +
            "AND JULIANDAY(due_date) - JULIANDAY((SELECT MAX(due_date) from tbTasksDone WHERE JULIANDAY(:date)-JULIANDAY(due_date) >= 31)) > 0")
    fun getLastMonthDoneTasks(date: String): LiveData<List<TaskDone>>

    /**
     * For deleting the done tasks older than one month
     */
    @Query("DELETE FROM tbTasksDone WHERE JULIANDAY(:currentDate) - JULIANDAY(due_date) > 30")
    fun deleteDoneTasksOlderThanOneMonth(currentDate: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskIntoDoneTable(task: TaskDone)
}