package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.CanvasView;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.BlendModeUtil;
import me.ccrama.redditslide.util.FileUtil;


/**
 * Created by ccrama on 5/27/2015.
 */
public class Draw extends BaseActivity implements ColorChooserDialog.ColorCallback {

    public static Uri uri;
    public static DoEditorActions editor;
    CanvasView drawView;
    View color;
    Bitmap bitmap;
    boolean enabled;
    private final ActivityResultLauncher<CropImageContractOptions> cropImageLauncher =
            registerForActivityResult(new CropImageContract(), this::cropImageResult);

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
        final CropImageContractOptions options = new CropImageContractOptions(uri, new CropImageOptions())
                .setGuidelines(CropImageView.Guidelines.ON);
        cropImageLauncher.launch(options);
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
        if (id == R.id.done && enabled) {
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

    private void cropImageResult(final CropImageView.CropResult result) {
        if (result.isSuccessful()) {
            bitmap = result.getBitmap(this)
                    .copy(Bitmap.Config.RGB_565, true);
            BlendModeUtil.tintDrawableAsModulate(color.getBackground(), getLastColor());
            color.setOnClickListener(v ->
                    new ColorChooserDialog.Builder(Draw.this, R.string.choose_color_title)
                            .allowUserColorInput(true)
                            .show(Draw.this));
            drawView.drawBitmap(bitmap);
            drawView.setPaintStrokeColor(getLastColor());
            drawView.setPaintStrokeWidth(20f);
            enabled = true;
        } else {
            finish();
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        drawView.setPaintStrokeColor(selectedColor);
        BlendModeUtil.tintDrawableAsModulate(color.getBackground(), selectedColor);

        Reddit.colors.edit().putInt("drawColor", selectedColor).commit();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }
}
