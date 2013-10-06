/*
 * OsmBrowser.java
 * LinuxDayOSM
 * Copyright (C) Stefano Salvi 2010 <stefano@salvi.mn.it>
 *
 * LinuxDayOSM is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LinuxDayOSM is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.mn.salvi.linuxDayOSM;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ZoomControls;

/**
 * @author salvi
 *
 *
 * Text size -> 12 at mdpi;
 */

public class OsmBrowser extends View implements OnScaleGestureListener, OnGestureListener {
    static final int tileSize = 256;
    static final int TOP = 5;
    static final int LEFT = 5;
    static final int SPACE = 3;
    
    static final int FONTSIZE=12;
    static final int MDPI=160;

    Thread loaderThread;
	private ArrayList<TagDescription> descs;
    private AsyncTask<String, Void, String> loaderTask = null;
    private TagDescription closeList;

    GeoTag tagList = null;

    private int oldZoom;
    private Point oldTile;
    
    private int dpi;

    int tileZoom;
    private Point firstTile;
    private Point screenCorner;
    Point absTopLeft;
    Point absBottomRight;

    private Point startDrag;
    private Point startAbsolutePixel;
    private int startZoom;

    private Dimension tilesSize;
    private Dimension screenDim;
    private Bitmap[][] tiles;

    private Paint mPaint;
    private ZoomControls mZoomControls;
    
    private String tilesDir;
    
    
    private ScaleGestureDetector mScaleDetector;    
    private GestureDetector mDetector;    
    private boolean mIsScrolling = false;
    
    private int legendaRight;
    private int legendaBotom;
    private ImageButton apriLegenda;
    private boolean legendaVisibile;
    
    public OsmBrowser(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        osmInitialyze(context, attrs);
    }

    public OsmBrowser(Context context, AttributeSet attrs) {
        super(context, attrs);
        osmInitialyze(context, attrs);
    }

    void setZoomButtons (ZoomControls mZoomControls) {
        this.mZoomControls = mZoomControls;
        mZoomControls.setOnZoomInClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (tileZoom < 18) {
					startDrag = new Point (screenDim.width/2, screenDim.height/2);
					startAbsolutePixel = screenToAbsolutePixel(startDrag);
					tileZoom ++;
					if (tileZoom == 18) {
						OsmBrowser.this.mZoomControls.setIsZoomInEnabled(false);
					}
					OsmBrowser.this.mZoomControls.setIsZoomOutEnabled(true);
					zoomReposition();
				}
			}
		});
        mZoomControls.setOnZoomOutClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (tileZoom > 2) {
					startDrag = new Point (screenDim.width/2, screenDim.height/2);
					startAbsolutePixel = screenToAbsolutePixel(startDrag);
					tileZoom --;
					if (tileZoom == 2) {
						OsmBrowser.this.mZoomControls.setIsZoomOutEnabled(false);
					}
					OsmBrowser.this.mZoomControls.setIsZoomInEnabled(true);
					zoomReposition();
				}
			}
		});
    }

    private void osmInitialyze (Context context, AttributeSet attrs) {
        tilesSize = new Dimension();
        tilesSize.width = 2;
        tilesSize.height = 2;
        tiles = new Bitmap[tilesSize.height][tilesSize.width];
        tileZoom = 2;
        oldZoom = 0;
        firstTile = new Point();
        firstTile.x = 0;
        firstTile.y = 0;
        oldTile = new Point();
        screenCorner = new Point();
        mPaint = new Paint();
        mScaleDetector = new ScaleGestureDetector(context, this);
        mDetector = new GestureDetector(context, this);
        descs = new ArrayList<TagDescription>();
        Resources res = getResources();
        closeList = new TagDescription(res.getString(R.string.CloseLegendaDescription), 
        			new PositionIcon(0.5, 0.5, BitmapFactory.decodeResource(res,R.drawable.icona_cancella)), this.toString());
        closeList.setActive(true);
        
        System.out.println ("Dimensione " + tilesSize.width + "X" + tilesSize.height + " tessere");

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OSMView);
        a.recycle();
   }

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		double zoomFactor = detector.getScaleFactor();
		if (zoomFactor > 1) {
			zoomFactor -= 1;
		} else {
			zoomFactor = 1-(1/zoomFactor);
		}
        tileZoom = (int) (zoomFactor + startZoom);
        if (tileZoom < 2) {
                tileZoom = 2;
        }
        if (tileZoom > 18) {
                tileZoom = 18;
        }
        mZoomControls.setIsZoomOutEnabled(tileZoom != 2);
        mZoomControls.setIsZoomInEnabled(tileZoom != 18);
        if (detector.isInProgress()) {
        	postInvalidate();
        	return false;
        } else {
            zoomReposition ();
        	return true;
        }
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		startDrag = new Point ((int)Math.floor(detector.getFocusX()), (int)Math.floor(detector.getFocusX()));
		startAbsolutePixel = screenToAbsolutePixel(startDrag);
		startZoom = tileZoom;
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		zoomReposition ();
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override  
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)  
	{  
		Log.i("OsmBrowser", "onScroll " + distanceX + "," + distanceY + " " + e1.getAction());
		setScreenCorner(screenCorner.x + (int)Math.floor(distanceX) /* p1.x - p2.x */, screenCorner.y + (int)Math.floor(distanceY)/* p1.y - p2.y */);
		postInvalidate();
		mIsScrolling = true;
		return false;  
	}  

	@Override  
	public void onShowPress(MotionEvent e)  
	{  
	}  

	@Override  
	public boolean onSingleTapUp(MotionEvent event)  
	{
		Point p = new Point ((int)Math.floor(event.getX()), (int)Math.floor(event.getY()));
		if (legendaVisibile && p.x < legendaRight && p.y < legendaBotom) {	// click nella legenda
			int y=TOP;
			int i;
			for (i=0; i < descs.size(); i++) {
				TagDescription d = descs.get(i);
				int altezza = d.getIcon().getSize().height + SPACE;
				if (p.y >= y && p.y < y+altezza) {
					d.setActive(!d.isActive());
					break;
				}
				y += altezza;
			}
			if (i == descs.size()) {
				legendaVisibile = false;
				apriLegenda.setVisibility(View.VISIBLE);
			}
			postInvalidate();
		} else {
			Point abs = screenToAbsolutePixel(p);
			GeoTag clicked=null;
			int scale = 1 << (18 - tileZoom);
			// Log.i("OsmBrowser", "onSingleTapUp " + p.x + "," + p.y + " " + abs.x + "," + abs.y + " - " + scale);
			for (GeoTag current=tagList; current!=null; current = current.getNext()) {
				if (current.isHit(abs, scale)) {
					clicked = current;
				}
			}
			if (clicked != null) {
				Log.i("OsmBrowser", "onSingleTapUp action");
				clicked.action(getContext(), p);
			}
		}
		return false;  
	}  

	public void setTilesDir (String tilesDir) {
		this.tilesDir = tilesDir;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		tilesSize.width = (w + tileSize - 1) / tileSize + 2;
		tilesSize.height = (h + tileSize - 1) / tileSize + 2;
		screenDim.width = w;
		screenDim.height = h;

		tiles = new Bitmap[tilesSize.height][tilesSize.width];
		oldZoom=0;
		loadTiles ();
	}

	void rediectedTuochEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP) {
			if(mIsScrolling) {
				mIsScrolling  = false;
				firstTile.y += screenCorner.y / tileSize;
				firstTile.x += screenCorner.x / tileSize;
				setScreenCorner(screenCorner.x % tileSize, screenCorner.y % tileSize);
				adjustAndLoad ();
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getPointerCount() > 1) {
			return mScaleDetector.onTouchEvent(event);
		}
		// mScaleDetector.onTouchEvent(event);
		if (mDetector.onTouchEvent(event)) {//return the double tap events  
			return true;
		}
		rediectedTuochEvent(event);
		return true;
	}

	private int printLabel (int x, int y, int displacement, int right, Canvas canvas, TagDescription d) {
		if (!d.isActive()) {
			mPaint.setColor(Color.GRAY);
			mPaint.setAlpha(64);
			canvas.drawRect(0, y - SPACE/2, right, y + d.getIcon().getSize().height + SPACE/2, mPaint);
			mPaint.setColor(Color.BLACK);
		}
		mPaint.setAlpha(d.isActive() ? 255 : 64);
		canvas.drawBitmap(d.getIcon().getIcon(), x, y, mPaint);
		canvas.drawText(d.getDescription(), (float)x+d.getIcon().getSize().width + 2, (float)(y - displacement + (d.getIcon().getSize().height / 2)), mPaint);
		return y + d.getIcon().getSize().height + SPACE;	   
	}

	@Override
	public void onDraw(Canvas canvas) {
		int x = 0;
		int y = 0;
		super.onDraw(canvas);
		for (y = (screenCorner.y-tileSize+1)/tileSize; (y * tileSize - screenCorner.y) < screenDim.height; y++) {
			for (x = (screenCorner.x-tileSize+1)/tileSize; (x * tileSize - screenCorner.x) < screenDim.width; x++) {
				if (x < tilesSize.width && y < tilesSize.height && x >= 0 && y >= 0 && tiles[y][x] != null) {
					canvas.drawBitmap(tiles[y][x], x * tileSize - screenCorner.x, y * tileSize - screenCorner.y, mPaint);
				} else {
					mPaint.setColor(Color.GRAY);
					mPaint.setStyle(Style.FILL);
					canvas.drawRect(x * tileSize - screenCorner.x, y * tileSize - screenCorner.y, x * tileSize - screenCorner.x + tileSize, y * tileSize - screenCorner.y + tileSize, mPaint);
					mPaint.setColor(Color.BLACK);
				}
			}
		}
		// System.out.println("Text Size = " + mPaint.getTextSize());
		mPaint.setTextSize(dpi*FONTSIZE/MDPI);
		int scale = 1 << (18 - tileZoom);
		// Log.i("Stdout", "TileZoom " + tileZoom + "Scale " + scale);
		x= LEFT;
		legendaBotom = TOP + closeList.getIcon().getSize().height + SPACE;
		legendaRight = x+closeList.getIcon().getSize().width + 2 + 
				(int)Math.ceil(mPaint.measureText(closeList.getDescription())) + LEFT + SPACE;
		descs.clear();
		for (GeoTag t = tagList; t != null; t = t.getNext()) {
			if (t.getDescription() != null && !descs.contains(t.getDescription())) {
				TagDescription d = t.getDescription();
				descs.add(d);
				legendaBotom += d.getIcon().getSize().height + SPACE;
				int width = x+d.getIcon().getSize().width + 2 + 
						(int)Math.ceil(mPaint.measureText(d.getDescription())) + LEFT + SPACE;
				if (width > legendaRight) {
					legendaRight = width;
				}
			}
			if (t.isActive()) {
				t.paint(canvas, mPaint, absTopLeft, absBottomRight, scale);
			}
		}
		if (legendaVisibile) {
			mPaint.setColor(Color.WHITE);	   
			mPaint.setStyle(Style.FILL);
			mPaint.setAlpha(192);
			canvas.drawRect(0, 0, legendaRight, legendaBotom, mPaint);
			mPaint.setColor(Color.BLACK);
			x=LEFT;
			y=TOP;
			int displacement = (int)Math.ceil(mPaint.getFontMetrics().ascent/2);
			for (int i=0; i < descs.size(); i++) {
				y = printLabel (x, y, displacement, legendaRight, canvas, descs.get(i));
			}
			y = printLabel (x, y, displacement, legendaRight, canvas, closeList);
		}

		/* canvas.drawText("Zoom : " + tileZoom, (float)10.0, (float)20.0, mPaint);
	   canvas.drawText("Coordinate tile : " + firstTile.x + "x" + firstTile.y, (float)10, (float)34, mPaint);
	   canvas.drawText("Dimensione array : " + tilesSize.width + "x" + tilesSize.height, (float)10, (float)48, mPaint);
	   canvas.drawText("Offset Schermo : " + screenCorner.x + "x" + screenCorner.y, (float)10, (float)62, mPaint);
	   canvas.drawText("Dimensione Schermo : " + screenDim.width + "x" + screenDim.height, (float)10, (float)76, mPaint);
	   canvas.drawText("Ultima tessera : " + x + "x" + y, (float)10, (float)90, mPaint); */
	}

	File createDataPath (int zoom, int x, int y) {
		String state = Environment.getExternalStorageState();

		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			return null;
		}
		if (tilesDir != null) {
			String FullPath = tilesDir + "/" + zoom;
			new File (FullPath).mkdirs();
			return new File (FullPath + "/" + x + "." + y +".png");
		}
		return null;
	}

	Bitmap loadTile (int x, int y, int tileZoom, int maxTiles) {
		if (x >= 0 && y >= 0 && x < maxTiles && y < maxTiles) {   		
			File tilePath = createDataPath (tileZoom,x,y);
			if (tilePath != null && tilePath.exists()) {
				return BitmapFactory.decodeFile(tilePath.getAbsolutePath());
			} else {
				try {
					URL imageURL = new URL(getTileNURL(x, y, tileZoom));
					HttpURLConnection conn = (HttpURLConnection)imageURL.openConnection();
					conn.setDoInput(true);
					conn.connect();
					InputStream is = conn.getInputStream();
					Bitmap tile = BitmapFactory.decodeStream(is);
					if (tilePath != null) {
						try {
							FileOutputStream fo = new FileOutputStream(tilePath);
							tile.compress(Bitmap.CompressFormat.PNG, 100, fo);
							fo.close();
						} catch (Exception e) {
						}
					}
					return tile;
				} catch (Exception e) {
					return null;
				}
			}
		}
		return null;
	}

	public void loadTask () {
		int maxTiles = 1 << tileZoom;
		int startx;
		int endx;
		int stepx;
		int starty;
		int endy;
		int stepy;
		/* valuta la direzione della copia in X */
		if (firstTile.x > oldTile.x) {
			startx = 0;
			endx = tilesSize.width;
			stepx = 1;
		} else {
			startx = tilesSize.width - 1;
			endx = -1;
			stepx = -1;
		}
		/* Valuta la direzione della copia in y */
		if (firstTile.y > oldTile.y) {
			starty = 0;
			endy = tilesSize.height;
			stepy = 1;
		} else {
			starty = tilesSize.height - 1;
			endy = -1;
			stepy = -1;
		}
		/* Se ci sono sovrapposizioni, sposta, se no annulla */
		for (int y = starty; y != endy; y+=stepy) {
			for (int x = startx; x != endx; x+= stepx) {
				/* if (oldZoom == tileZoom && (x + firstTile.x) >= oldTile.x && (x + firstTile.x) < (oldTile.x + tilesSize.width) &&
						   (y + firstTile.y) >= oldTile.y && (y + firstTile.y) < (oldTile.y + tilesSize.height)) {
					   tiles[y][x] = tiles[y + firstTile.y - oldTile.y][x + firstTile.x - oldTile.x];
				   } else { */
				tiles[y][x]=null;
				// }
			}
		}
		/* Carica le tessere vuote */
		for (int y = 0; y < tilesSize.height && Thread.currentThread() == loaderThread; y++) {
			for (int x = 0; x < tilesSize.width && Thread.currentThread() == loaderThread; x++) {
				if ((tiles[y][x] = loadTile (firstTile.x + x, firstTile.y + y, tileZoom, maxTiles)) != null) {
					postInvalidate();
				}
			}
		}
		postInvalidate();
		oldZoom = tileZoom;
		oldTile = new Point (firstTile);
		loaderThread = null;
		Log.i("Stdout","Fine LoadTiles");

	}

	void zoomReposition () {
		int mult = 1 << (18 - tileZoom);
		int scaledX = (startAbsolutePixel.x / mult) - startDrag.x;
		int scaledY = (startAbsolutePixel.y / mult) - startDrag.y;
		firstTile.x = (scaledX - tileSize/2)/tileSize;
		firstTile.y = (scaledY - tileSize/2)/tileSize;
		setScreenCorner(scaledX - (firstTile.x * tileSize), scaledY - (firstTile.y * tileSize));
		adjustAndLoad ();
	}

	int distance (MotionEvent event) {
		double dx = event.getX(1) - event.getX(0);
		double dy = event.getY(0) - event.getY(1);
		return (int)Math.round(Math.sqrt(dx*dx+dy*dy));
	}

	public static String getTileNURL(final int xtile, final int ytile, final int zoom) {
		return("http://c.tile.openstreetmap.org/" + zoom + "/" + xtile + "/" + ytile + ".png");
	}

	public void setTags (GeoTag tagList) {
		this.tagList = tagList;
		postInvalidate();
	}

	void centerPoint (double lat, double lon, int zoom) {
		tileZoom = zoom;
		int ratio = 1 << (18 - tileZoom);
		int left = long2absolutex (lon)/ratio - (screenDim.width/2);
		int top = lat2absolutey (lat)/ratio - (screenDim.height/2);
		if (left < 0) {
			left = 0;
		}
		if (top < 0) {
			top = 0;
		}
		firstTile.x = (left - tileSize/2)/tileSize;
		if ((left%tileSize) == 0) {
			firstTile.x --;
		}
		firstTile.y = (top - tileSize/2)/tileSize;
		if ((top%tileSize) == 0) {
			firstTile.y --;
		}
		setScreenCorner (left - (firstTile.x * tileSize), top - (firstTile.y * tileSize));
		oldZoom = 0;
		if (mZoomControls != null) {
			mZoomControls.setIsZoomInEnabled(true);
			mZoomControls.setIsZoomOutEnabled(true);
		}
	}

	public void centerArea (double topLat, double leftLon, double bottomLat, double rightLon) {
		int left = long2absolutex (leftLon);
		int top = lat2absolutey (topLat);
		int right = long2absolutex (rightLon);
		int bottom = lat2absolutey (bottomLat);
		double scaleX = (double) (right - left)/screenDim.width;
		double scaleY = (double) (bottom - top)/screenDim.height;
		double scale = (scaleX > scaleY) ? scaleX : scaleY;
		tileZoom = 18 - (int)Math.ceil(Math.log(scale) / Math.log(2.0));
		int ratio = 1 << (18 - tileZoom);
		left /= ratio;
		top /= ratio;
		right /= ratio;
		bottom /= ratio;
		left -= (screenDim.width - right + left) / 2;
		top -= (screenDim.height - bottom + top) / 2;
		firstTile.x = (left - tileSize/2)/tileSize;
		firstTile.y = (top - tileSize/2)/tileSize;
		setScreenCorner (left - (firstTile.x * tileSize), top - (firstTile.y * tileSize));
		oldZoom = 0;
		if (mZoomControls != null) {
			mZoomControls.setIsZoomInEnabled(true);
			mZoomControls.setIsZoomOutEnabled(true);
		}
		adjustAndLoad ();
		System.out.println ("Scala X " + scaleX + "Scala Y " + scaleY + " Scala " + scale + " Livello " + tileZoom + " firsr Tile " + firstTile.x + "x" + firstTile.y + " Left Corner " + screenCorner.x + "x" + screenCorner.y);
	}

	private class LoadTilesOperation extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			int maxTiles = 1 << tileZoom;
			int startx;
			int endx;
			int stepx;
			int starty;
			int endy;
			int stepy;
			/* valuta la direzione della copia in X */
			if (firstTile.x > oldTile.x) {
				startx = 0;
				endx = tilesSize.width;
				stepx = 1;
			} else {
				startx = tilesSize.width - 1;
				endx = -1;
				stepx = -1;
			}
			/* Valuta la direzione della copia in y */
			if (firstTile.y > oldTile.y) {
				starty = 0;
				endy = tilesSize.height;
				stepy = 1;
			} else {
				starty = tilesSize.height - 1;
				endy = -1;
				stepy = -1;
			}
			/* Se ci sono sovrapposizioni, sposta, se no annulla */
			for (int y = starty; y != endy; y+=stepy) {
				for (int x = startx; x != endx; x+= stepx) {
					if (oldZoom == tileZoom && (x + firstTile.x) >= oldTile.x && (x + firstTile.x) < (oldTile.x + tilesSize.width) &&
							(y + firstTile.y) >= oldTile.y && (y + firstTile.y) < (oldTile.y + tilesSize.height)) {
						tiles[y][x] = tiles[y + firstTile.y - oldTile.y][x + firstTile.x - oldTile.x];
					} else {
						tiles[y][x]=null;
					}
				}
			}
			/* Carica le tessere vuote */
			for (int y = 0; y < tilesSize.height /* && this == loaderTask */; y++) {
				for (int x = 0; x < tilesSize.width /* && this == loaderTask */; x++) {
					if (y < tiles.length && x < tiles[y].length && (tiles[y][x] = loadTile (firstTile.x + x, firstTile.y + y, tileZoom, maxTiles)) != null) {
						postInvalidate();
					}
				}
			}
			postInvalidate();
			oldZoom = tileZoom;
			oldTile = new Point (firstTile);
			loaderTask = null;
			Log.i("Stdout","Fine LoadTiles");
			return null;
		}

		@Override
		protected void onPostExecute(String result) {               
			postInvalidate();
		}
	}

	void loadTiles () {
		Log.i("stdout", "LoadTiles (" + ((loaderTask != null) ? "Interrompo" : "Nuovo") + ")");
		loaderTask = new LoadTilesOperation().execute("");
		// loaderThread = new Thread(this);
		// loaderThread.start();
	}

	void adjustAndLoad () {
		if (screenCorner.y < tileSize/2) {
			screenCorner.y += tileSize;
			firstTile.y --;
		}
		if (screenCorner.x < tileSize/2) {
			screenCorner.x += tileSize;
			firstTile.x --;
		}
		loadTiles ();
	}

	//lat	lon	title	description	iconSize	iconOffset	icon
	// 5193501.5396258	1601689.3235986	AnxaLug	<a href="http://www.anxalug.org/">http://www.anxalug.org/</a>	16,19	-8,-19	http://lugmap.it/images/icon.png
	public static String[] tabParser (String csv) {
		ArrayList<String> found = new ArrayList<String>();
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < csv.length(); i++) {
			if (csv.charAt(i) == '\t') {      // Separator
				found.add(s.toString());
				s = new StringBuilder();
			} else {
				while (i<csv.length() && csv.charAt(i)!='\t') {
					s.append(csv.charAt(i++));
				}
				if (i<csv.length() && csv.charAt(i)=='\t') {
					i--;
				}
			}
		}
		found.add(s.toString());
		String[] res = new String[found.size()];
		res = found.toArray(res);
		return res;
	}

	public static String[] csvParser (String csv) {
		ArrayList<String> found = new ArrayList<String>();
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < csv.length(); i++) {
			while (i<csv.length() && (csv.charAt(i)==' ' || csv.charAt(i)=='\t')) {
				i++;
			}
			if (csv.charAt(i)=='"') {       // Quoted string
				i++;
				while (i<csv.length() && csv.charAt(i)!='"') {
					if (csv.charAt(i)=='\\') {
						i++;
					}
					if (i<csv.length()) {
						s.append(csv.charAt(i++));
					}
				}
				if (i<csv.length()) {
					i++;
				}
				while (i<csv.length() && (csv.charAt(i)==' ' || csv.charAt(i)=='\t')) {
					i++;
				}
				if (i<csv.length() && csv.charAt(i)==',') {
					i--;
				}
			} else if (csv.charAt(i) == ',') {      // Separator
				found.add(s.toString());
				s = new StringBuilder();
			} else {
				while (i<csv.length() && csv.charAt(i)!=' ' && csv.charAt(i)!='\t' && csv.charAt(i)!=',') {
					s.append(csv.charAt(i++));
				}
				while (i<csv.length() && (csv.charAt(i)==' ' || csv.charAt(i)=='\t')) {
					i++;
				}
				if (i<csv.length() && csv.charAt(i)==',') {
					i--;
				}
			}
		}
		found.add(s.toString());
		String[] res = new String[found.size()];
		res = found.toArray(res);
		return res;
	}

	int long2tilex(double lon)
	{
		return (int)(Math.floor((lon + 180.0) / 360.0 * (1<<tileZoom)));
	}

	int lat2tiley(double lat)
	{
		return (int)(Math.floor((1.0 - Math.log( Math.tan(lat * Math.PI/180.0) + 1.0 / Math.cos(lat * Math.PI/180.0)) / Math.PI) / 2.0 * (1<<tileZoom)));
	}

	public static int long2absolutex(double lon)
	{
		return (int)(Math.floor((lon + 180.0) / 360.0 * (1<<26)));
	}

	public static int lat2absolutey(double lat)
	{
		return (int)(Math.floor((1.0 - Math.log( Math.tan(lat * Math.PI/180.0) + 1.0 / Math.cos(lat * Math.PI/180.0)) / Math.PI) / 2.0 * (1<<26)));
	}

	double tilex2long(double x)
	{
		return x / (double)(1<<tileZoom) * 360.0 - 180;
	}

	double tiley2lat(double y)
	{
		double n = Math.PI - 2.0 * Math.PI * y / (double)(1<<tileZoom);
		return 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
	}

	GeoPoint screenToPoint (Point s) {
		GeoPoint g = new GeoPoint();
		double x = (double)(s.x + screenCorner.x)/tileSize;
		double y = (double)(s.y + screenCorner.y)/tileSize;
		g.lon = tilex2long(x + firstTile.x);
		g.lat = tiley2lat(y + firstTile.y);
		return g;
	}

	Point screenToAbsolutePixel (Point s) {
		Point a = new Point();
		int mult = 1 << (18 - tileZoom);
		a.x = (firstTile.x*tileSize + s.x + screenCorner.x) * mult + mult / 2;
		a.y = (firstTile.y*tileSize + s.y + screenCorner.y) * mult + mult / 2;
		return a;
	}

	void setScreenCorner (int x, int y) {
		screenCorner.y = y;
		screenCorner.x = x;
		absTopLeft = screenToAbsolutePixel(new Point(0,0));
		absBottomRight = screenToAbsolutePixel(new Point (screenDim.width, screenDim.height));
	}

	public void setOpenLegenda(ImageButton openLegenda) {
		this.apriLegenda = openLegenda;
		legendaVisibile = false;
		// openLegenda.setVisibility(View.GONE);
		openLegenda.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				v.setVisibility(View.GONE);
				legendaVisibile = true;
				OsmBrowser.this.postInvalidate();
			}
		});
	}

	public void setDisplayMetrics(DisplayMetrics metrics) {
		dpi = metrics.densityDpi;
        screenDim = new Dimension(metrics.widthPixels, metrics.heightPixels);		
        setScreenCorner (tileSize, tileSize);
        loadTiles ();
	}
}
