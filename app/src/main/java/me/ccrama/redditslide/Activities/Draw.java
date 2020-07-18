package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.CanvasView;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.FileUtil;


/**
 * Created by ccrama on 5/27/2015.
 */
public class Draw extends BaseActivity implements ColorChooserDialog.ColorCallback {

    CanvasView drawView;
    View       color;
    Bitmap     bitmap;
    public static Uri             uri;
    public static DoEditorActions editor;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();
        disableSwipeBackLayout();
        super.onCreate(savedInstance);
        applyColorTheme("");
        setContentView(R.layout.activity_draw);
        drawView = (CanvasView) findViewById(R.id.paintView);
        drawView.setBaseColor(Color.parseColor("#303030"));
        color = findViewById(R.id.color);
        CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setupAppBar(R.id.toolbar, "", true, Color.parseColor("#212121"), R.id.toolbar);
    }

    public int getLastColor() {
        return Reddit.colors.getInt("drawColor", Palette.getDefaultAccent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        if(id == R.id.done && enabled){
            File image; //image to share
            //check to see if the cache/shared_images directory is present
            final File imagesDir = new File(
                    Draw.this.getCacheDir().toString() + File.separator + "shared_image");
            if (!imagesDir.exists()) {
                imagesDir.mkdir(); //create the folder if it doesn't exist
            } else {
                FileUtil.deleteFilesInDir(imagesDir);
            }

            try {
                //creates a file in the cache; filename will be prefixed with "img" and end with ".png"
                image = File.createTempFile("img", ".png", imagesDir);
                FileOutputStream out = null;

                try {
                    //convert image to png
                    out = new FileOutputStream(image);
                    Bitmap.createBitmap(drawView.getBitmap(), 0, (int) drawView.height,
                            (int) drawView.right, (int) (drawView.bottom - drawView.height))
                            .compress(Bitmap.CompressFormat.JPEG, 100, out);
                } finally {
                    if (out != null) {
                        out.close();

                        final Uri contentUri = FileUtil.getFileUri(image, this);
                        if (contentUri != null) {
                            Intent intent = FileUtil.getFileIntent(image, new Intent(), this);
                            setResult(RESULT_OK, intent);
                        } else {
                            //todo error Toast.makeText(this, getString(R.string.err_share_image), Toast.LENGTH_LONG).show();
                        }
                        finish();
                    }
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                //todo error Toast.makeText(this, getString(R.string.err_share_image), Toast.LENGTH_LONG).show();
            }
        }
        if (id == R.id.undo) {
            drawView.undo();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.draw_menu, menu);
        return true;
    }

    boolean enabled ;

    @Override
    protected void onActivityResult(int code, int resultC, Intent data) {
        super.onActivityResult(code, resultC, data);
        if (code == 10001 && data != null) {
            Uri selectedImageUri = data.getData();
            CropImage.activity(selectedImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        } else if (data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri selectedImageUri = result.getUri();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri)
                        .copy(Bitmap.Config.RGB_565, true);
                color.getBackground().setColorFilter(getLastColor(), PorterDuff.Mode.MULTIPLY);
                color.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ColorChooserDialog.Builder(Draw.this,
                                R.string.choose_color_title).allowUserColorInput(true).show();
                    }
                });
                drawView.drawBitmap(bitmap);
                drawView.setPaintStrokeColor(getLastColor());
                drawView.setPaintStrokeWidth(20f);
                enabled = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            finish();
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        drawView.setPaintStrokeColor(selectedColor);
        color.getBackground().setColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY);

        Reddit.colors.edit().putInt("drawColor", selectedColor).commit();
    }
}
