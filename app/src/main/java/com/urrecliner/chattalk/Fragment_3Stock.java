package com.urrecliner.chattalk;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.urrecliner.chattalk.Vars.aBar;
import static com.urrecliner.chattalk.Vars.logSave;
import static com.urrecliner.chattalk.Vars.logStock;
import static com.urrecliner.chattalk.Vars.mActivity;
import static com.urrecliner.chattalk.Vars.mContext;
import static com.urrecliner.chattalk.Vars.sharedEditor;
import static com.urrecliner.chattalk.Vars.tableFolder;
import static com.urrecliner.chattalk.Vars.topTabs;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.urrecliner.chattalk.Sub.LogString;
import com.urrecliner.chattalk.Sub.SnackBar;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class Fragment_3Stock extends Fragment {

    ViewGroup rootView;
    ScrollView scrollView1;
    SpannableString ss, sv;
    EditText etTable, etKeyword;
    ImageView ivFind, ivClear, ivNext;
    Menu mainMenu;
    int logPos = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(
                R.layout.frag3_stock, container, false);
        etTable = rootView.findViewById(R.id.text_stock);
        etKeyword = rootView.findViewById(R.id.key_stock);
        ivFind = rootView.findViewById(R.id.find_stock);
        ivNext = rootView.findViewById(R.id.next_stock);
        ivClear = rootView.findViewById(R.id.clear_stock);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onResume() {
        topTabs.getTabAt(3).select();
        logStock = logStock.replace("    ","");
        ss = new LogString().make(logStock, mContext);
        sv = ss;
        etTable.setText(ss);
        etTable.setFocusableInTouchMode(true);
        etTable.setFocusable(true);

        ivNext.setVisibility(View.GONE);
        logPos = -1;
        ivFind.setOnClickListener(v -> {
            String key = etKeyword.getText().toString();
            if (key.length() < 2)
                return;
            int cnt = 0;
            logPos = -1;
            String fullText = etTable.getText().toString();
            ss = sv;
            int oEnd = fullText.indexOf(key);
            for (int oStart = 0; oStart < fullText.length() && oEnd != -1; oStart = oEnd + 2) {
                oEnd = fullText.indexOf(key, oStart);
                if (oEnd > 0) {
                    cnt++;
                    if (logPos < 0)
                        logPos = oEnd;
                    ss.setSpan(new BackgroundColorSpan(0xFFFFFF00), oEnd, oEnd + key.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            sv = ss;
            etTable.setText(ss);
            new SnackBar().show(key, cnt+" times Found");
            Editable etText = etTable.getText();
            if (logPos > 0) {
                Selection.setSelection(etText, logPos);
                etTable.requestFocus();
                ivNext.setVisibility(View.VISIBLE);
            }
        });

        ivNext.setOnClickListener(v -> {
            String key = etKeyword.getText().toString();
            if (key.length() < 2)
                return;
            Editable etText = etTable.getText();
            String s = etText.toString();
            logPos = s.indexOf(key, logPos+1);
            if (logPos > 0) {
                Selection.setSelection(etText, logPos);
                etTable.requestFocus();
            }
        });

        ivClear.setOnClickListener(v -> etKeyword.setText(""));
        scrollView1 = rootView.findViewById(R.id.scroll_3_stock);
        new Handler(Looper.getMainLooper()).post(() -> scrollView1.smoothScrollBy(0, 10000));
        super.onResume();

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        mainMenu = menu;
        inflater.inflate(R.menu.menu_3stock, menu);
        super.onCreateOptionsMenu(menu, inflater);
        aBar.setTitle(" Stock");
        aBar.setSubtitle(null);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.delete_item_stock) {
            delete_OneItem();

        } else if (item.getItemId() == R.id.action_restock) {
            reload_stock();

        } else if (item.getItemId() == R.id.delete_1line_stock) {
            delete_OneLine();

        } else if (item.getItemId() == R.id.copy2stock) {
            String logNow = etTable.getText().toString().trim() + "\n";
            int ps = logNow.lastIndexOf("\n", etTable.getSelectionStart() - 1);
            if (ps == -1)
                ps = 0;
            int pf = logNow.indexOf("\n", ps + 1);
            if (pf == -1)
                pf = logNow.length();

            ps = logNow.lastIndexOf("\n", ps - 1);
            if (ps == -1)
                ps = 0;
            else {
                ps = logNow.lastIndexOf("\n", ps - 1);
            }
            String copied = logNow.substring(ps, pf);
            logSave += copied;
            sharedEditor.putString("logSave", logSave);
            sharedEditor.apply();
            copied = copied.replace("\n", " 🗼️ ");
            Toast.makeText(mContext, "stock copied " + copied, Toast.LENGTH_SHORT).show();

        }
        return super.onOptionsItemSelected(item);
    }

    private void delete_OneLine() {
        Vars.DelItem delItem = new LogString().delLine(etTable.getText().toString(), etTable.getSelectionStart(), mContext);
        etTable.setText(delItem.ss);
        logStock = delItem.logNow;
        sharedEditor.putString("logStock", logStock);
        sharedEditor.apply();
        Editable etText = etTable.getText();
        Selection.setSelection(etText, delItem.ps, delItem.pf);
    }

    private void delete_OneItem() {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etTable.getWindowToken(), 0);

        Vars.DelItem delItem = new LogString().delItem(etTable.getText().toString(), etTable.getSelectionStart(), mContext);
        logStock = delItem.logNow;
        sharedEditor.putString("logStock", logStock);
        sharedEditor.apply();
        etTable.setText(delItem.ss);
        Editable etText = etTable.getText();
        Selection.setSelection(etText, delItem.ps, delItem.pf);
        etTable.requestFocus();
        scrollView1.post(() -> new Timer().schedule(new TimerTask() {
            public void run() {
                mActivity.runOnUiThread(() -> scrollView1.scrollBy(0, 100));
            }
        }, 30));
    }

    private void reload_stock() {
        String [] que = new FileIO().readKR(new File(tableFolder, "logStock.txt").toString());
        StringBuilder sb = new StringBuilder();
        for (String s: que) {
            sb.append(s).append("\n");
        }
        logStock = sb.toString();
        etTable.setText(new LogString().make(logStock, mContext));
    }
    OnBackPressedCallback callback;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback.remove();
    }
}