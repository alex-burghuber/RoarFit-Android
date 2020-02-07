package at.spiceburg.roarfit

import android.content.Context
import at.spiceburg.roarfit.data.db.AppDatabase
import at.spiceburg.roarfit.data.repositories.ExerciseRepository
import at.spiceburg.roarfit.data.repositories.UserRepository
import at.spiceburg.roarfit.data.repositories.WorkoutRepository
import at.spiceburg.roarfit.network.KeyFitApi
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * For manual dependency injection
 * @see <a href="https://developer.android.com/training/dependency-injection/manual">Manual dependency injection</a>
 */
class AppContainer(context: Context) {

    val keyFitApi: KeyFitApi = Retrofit.Builder()
        //.baseUrl("https://staging.key.fit/lionsoft/app/")
        .baseUrl("https://vm102.htl-leonding.ac.at/roarfit/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(KeyFitApi::class.java)

    var userRepository: UserRepository
    var exerciseRepository: ExerciseRepository
    var workoutRepository: WorkoutRepository

    init {
        val database = AppDatabase.getDatabase(context)
        val userDao = database.userDao()
        val workoutExerciseDao = database.workoutExerciseDao()

        userRepository = UserRepository(keyFitApi, userDao)
        exerciseRepository = ExerciseRepository(keyFitApi, workoutExerciseDao)
        workoutRepository = WorkoutRepository(keyFitApi, workoutExerciseDao)
    }
}
