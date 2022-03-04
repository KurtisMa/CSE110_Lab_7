package edu.ucsd.cse110.dogegotchi.doge;


import android.view.View;

public class MenuView implements IDogeObserver{
    private View menuView;

    public MenuView(View menuView){
        this.menuView = menuView;
    }

    @Override
    public void onStateChange(Doge.State newState){
        if (newState == Doge.State.SAD)
            menuView.setVisibility(View.VISIBLE);
        else
            menuView.setVisibility(View.GONE);
    }
}
