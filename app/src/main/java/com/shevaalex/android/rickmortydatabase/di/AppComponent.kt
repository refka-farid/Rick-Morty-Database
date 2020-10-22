package com.shevaalex.android.rickmortydatabase.di

import android.app.Application
import com.shevaalex.android.rickmortydatabase.ui.InitViewModel
import com.shevaalex.android.rickmortydatabase.ui.MainActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    NetworkModule::class,
    DbModule::class])
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): AppComponent
    }

    //inject into MainActivity
    fun inject(activity: MainActivity)

}