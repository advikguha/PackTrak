package com.algorhythms.infinity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;

import org.apache.http.protocol.HTTP;

import java.util.List;

/**
 * Allows you to export a trip via text, email, or anything else. <br />
 * This is NOT meant for production; must be integrated into another activity.
 * @author Algorhythms
 */
public class Exporter  {

    //private ItemDbAdapter mItemDbAdapter;
    //private CategoryDbAdapter mCategoryDbAdapter;
    //private TripsDbAdapter mTripsDbAdapter;
    private Context ctx;

    public Exporter(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Handy utility method for making Markdown-style headers.
     * @param c The character for the header.
     * @param q The copy factor.
     * @return The header string.
     */
    private static final String makeHeader(char c, int q) {
        StringBuilder sb = new StringBuilder(q);
        for (int i = 0; i < q; i++)
            sb.append(c);
        return sb.toString();
    }

    public void export(long tripid) {
        TripsDbAdapter mTripsDbAdapter = TripsDbAdapter.getInstance(ctx);
        CategoryDbAdapter mCategoryDbAdapter = CategoryDbAdapter.getInstance(ctx);
        ItemDbAdapter mItemDbAdapter = ItemDbAdapter.getInstance(ctx);

        Cursor c = mTripsDbAdapter.fetchTrip(tripid);
        //ctx.startManagingCursor(c);
        StringBuilder sb = new StringBuilder();

        // Get trip info
        String tripname = c.getString(c.getColumnIndexOrThrow(TripsDbAdapter.KEY_TRIPNAME));
        sb.append(tripname);
        sb.append('\n');
        sb.append(makeHeader('=', tripname.length()));
        sb.append('\n');
        sb.append("Starts on: " + c.getString(c.getColumnIndexOrThrow(TripsDbAdapter.KEY_STARTDATE)) + '\n');
        sb.append("Ends on: " + c.getString(c.getColumnIndexOrThrow(TripsDbAdapter.KEY_ENDDATE)) + '\n');
        sb.append('\n');
        c.close();

        Intent exportIntent = new Intent(Intent.ACTION_SEND);
        exportIntent.setType(HTTP.PLAIN_TEXT_TYPE);
        exportIntent.putExtra(Intent.EXTRA_SUBJECT, tripname);

        Cursor catcur, itcur;

        // TODO Build the message, jumping across categories
        catcur = mCategoryDbAdapter.fetchAllCategories(tripid);
        while (!catcur.isAfterLast()) {
            // Category name
            String catname = catcur.getString(catcur.getColumnIndexOrThrow(CategoryDbAdapter.KEY_CATNAME));
            long catid = catcur.getLong(catcur.getColumnIndexOrThrow(CategoryDbAdapter.KEY_ROWID));
            sb.append(catname);
            sb.append('\n');
            sb.append(makeHeader('-', catname.length()));
            sb.append('\n');

            // Get item data
            itcur = mItemDbAdapter.fetchAllItems(catid);
            while(!itcur.isAfterLast()) {
                sb.append(itcur.getString(itcur.getColumnIndexOrThrow(ItemDbAdapter.KEY_ITEMNAME)));
                sb.append(" x");
                sb.append(itcur.getInt(itcur.getColumnIndexOrThrow(ItemDbAdapter.KEY_QUANTITY)));
                sb.append('\n');
                itcur.moveToNext();
            }
            itcur.close();
            catcur.moveToNext();
        }
        catcur.close();

        exportIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());

        // Let's see if anyone can handle our intent
        // Verify to see that there's an app on this device that can handle the intent
        PackageManager packageManager = ctx.getPackageManager();
        List<ResolveInfo> apps = packageManager.queryIntentActivities(exportIntent, 0);

        // Launch the activity if safe; that is, there exists an app to handle the intent.
        // This prompts where to open this intent text
        if (apps.size() > 0) {
            // TODO Use string resources with UI stuff (just to be safe)
            //String title = getResources().getText(R.string.chooser_title).toString();
            String title = "Export your list with what app?";

            // Create and start an app chooser
            Intent chooser = Intent.createChooser(exportIntent, title);
            ctx.startActivity(chooser);
        }
        else {
            new AlertDialog.Builder(ctx)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Uh Hh!")
                    .setMessage("Unfortunately, you don't have any apps that can export your list.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ;
                        }
                    }).show();
        }
    }
}