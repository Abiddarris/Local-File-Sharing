package com.abiddarris.lanfileviewer.explorer;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Rect;
import android.view.View;
import android.view.View;
import android.graphics.drawable.Drawable;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.abiddarris.lanfileviewer.R;

public class ChevronItemDecoration extends RecyclerView.ItemDecoration {
    
    private Drawable chevronDrawable;
    private int chevronWidth;
    private int chevronHeight;
    
    public ChevronItemDecoration(Context context) {
        chevronDrawable = ContextCompat.getDrawable(context, R.drawable.ic_chevron_right);
        chevronHeight = chevronDrawable.getIntrinsicHeight();
        chevronWidth = chevronDrawable.getIntrinsicWidth();
    }
    
    @Override
    public void getItemOffsets(Rect rect, View view, RecyclerView parent, RecyclerView.State state) {
        Adapter adapter = parent.getAdapter();
        if(adapter == null) return;
        
        int pos = parent.getChildAdapterPosition(view);
        if(pos == RecyclerView.NO_POSITION) return;
        
        rect.right = (pos == adapter.getItemCount() - 1) ? 0 : chevronDrawable.getIntrinsicWidth();
    }
    
    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        Adapter adapter = parent.getAdapter();
        if(adapter == null) return;
        
        for(int i = 0; i < parent.getChildCount(); ++i) {
        	View view = parent.getChildAt(i);
            int pos = parent.getChildAdapterPosition(view);
            
            if(pos == RecyclerView.NO_POSITION) continue;
          
            if(pos != adapter.getItemCount() - 1) {
                int left = view.getRight();
                int right = left + chevronWidth;
                int top = parent.getPaddingTop();
                int padding = (view.getBottom() - top - chevronHeight) / 2;
               
                top += padding;
                
                int bottom = top + chevronHeight;
                
                chevronDrawable.setBounds(new Rect(left, top, right, bottom));
                chevronDrawable.draw(canvas);
            }
        }
    }
    
}
