import os
import subprocess

def generate_market_logo(output_path, size=512):
    # We will build an ImageMagick command that constructs the exact logo from the user's reference image.
    # Dimensions: 512x512
    cmd = [
        "convert",
        "-size", f"{size}x{size}",
        "xc:none",
        
        # 1. Base dark royal navy circular background
        "-fill", "#071633",
        "-draw", "circle 256,256 256,12",
        
        # 2. Subtle radial gradient effect in the top-center
        "-fill", "#0F2856",
        "-draw", "circle 256,220 256,80",
        
        # 3. Golden Sun Disc behind head
        "-fill", "#F59E0B",
        "-draw", "circle 256,170 256,70",
        "-fill", "#FCD34D",
        "-draw", "circle 256,170 256,90",
        
        # 4. Left Market Shop building (Navy wall + rolled shutter door + striped awning)
        "-fill", "#0A1D3F",
        "-draw", "polygon 35,140 185,140 185,320 35,320",
        # Roller shutter
        "-fill", "#172A52",
        "-draw", "polygon 50,190 170,190 170,310 50,310",
        "-stroke", "#0D1B36", "-strokewidth", "3",
        "-draw", "line 50,210 170,210 line 50,230 170,230 line 50,250 170,250 line 50,270 170,270 line 50,290 170,290",
        "-stroke", "none",
        # Striped Awning (Left)
        "-fill", "#0284C7",
        "-draw", "polygon 25,140 190,140 180,185 20,185",
        "-fill", "#38BDF8",
        "-draw", "polygon 40,140 70,140 60,185 30,185",
        "-draw", "polygon 100,140 130,140 120,185 90,185",
        "-draw", "polygon 160,140 190,140 180,185 150,185",
        # Scallop hem
        "-fill", "#38BDF8",
        "-draw", "circle 35,185 35,193 circle 65,185 65,193 circle 95,185 95,193 circle 125,185 125,193 circle 155,185 155,193",
        
        # 5. Right Market Shop building
        "-fill", "#0A1D3F",
        "-draw", "polygon 327,140 477,140 477,320 327,320",
        # Roller shutter
        "-fill", "#172A52",
        "-draw", "polygon 342,190 462,190 462,310 342,310",
        "-stroke", "#0D1B36", "-strokewidth", "3",
        "-draw", "line 342,210 462,210 line 342,230 462,230 line 342,250 462,250 line 342,270 462,270 line 342,290 462,290",
        "-stroke", "none",
        # Striped Awning (Right)
        "-fill", "#0284C7",
        "-draw", "polygon 322,140 487,140 492,185 332,185",
        "-fill", "#38BDF8",
        "-draw", "polygon 322,140 352,140 362,185 332,185",
        "-draw", "polygon 382,140 412,140 422,185 392,185",
        "-draw", "polygon 442,140 472,140 482,185 452,185",
        # Scallop hem
        "-fill", "#38BDF8",
        "-draw", "circle 357,185 357,193 circle 387,185 387,193 circle 417,185 417,193 circle 447,185 447,193 circle 477,185 477,193",
        
        # 6. Center Portrait: Naseeb Lal Ji (Revered Elder)
        # Shoulders & White Kurta Torso
        "-fill", "#FFFFFF",
        "-draw", "polygon 105,380 407,380 375,230 256,220 137,230",
        # Kurta folds and soft shading
        "-fill", "#E2E8F0",
        "-draw", "polygon 125,380 155,380 145,260 135,260",
        "-draw", "polygon 357,380 387,380 377,260 367,260",
        # Collar curve
        "-fill", "#F1F5F9",
        "-draw", "ellipse 256,225 35,15 0 360",
        
        # Neck
        "-fill", "#C07E4B",
        "-draw", "ellipse 256,200 32,35 0 360",
        
        # Head / Face
        "-fill", "#D49360",
        "-draw", "ellipse 256,155 72,88 0 360",
        
        # Forehead & Cheek Highlights
        "-fill", "#DE9F6E",
        "-draw", "ellipse 256,135 55,40 0 360",
        
        # Hair (Salt & Pepper / Graying hair framing head)
        "-fill", "#666666",
        "-draw", "ellipse 256,92 74,40 180 360",
        "-draw", "ellipse 186,140 14,35 180 360",
        "-draw", "ellipse 326,140 14,35 180 360",
        "-fill", "#999999",
        "-draw", "ellipse 256,86 65,30 180 360",
        
        # Sacred Red Tilak / Bindi on forehead
        "-fill", "#DC2626",
        "-draw", "circle 256,105 256,115",
        "-fill", "#FBBF24",
        "-draw", "circle 256,105 256,108",
        
        # Eyebrows
        "-fill", "#555555",
        "-draw", "ellipse 228,125 18,5 170 350",
        "-draw", "ellipse 284,125 18,5 190 10",
        
        # Eyes
        "-fill", "#FFFFFF",
        "-draw", "ellipse 228,135 13,7 0 360",
        "-draw", "ellipse 284,135 13,7 0 360",
        "-fill", "#3B1E08",
        "-draw", "circle 228,135 228,140",
        "-draw", "circle 284,135 284,140",
        "-fill", "#000000",
        "-draw", "circle 228,135 228,138",
        "-draw", "circle 284,135 284,138",
        "-fill", "#FFFFFF",
        "-draw", "circle 226,133 226,135",
        "-draw", "circle 282,133 282,135",
        
        # Nose
        "-stroke", "#A06233", "-strokewidth", "3", "-fill", "#C07E4B",
        "-draw", "line 256,128 253,160 line 253,160 259,160",
        "-stroke", "none",
        "-fill", "#A06233",
        "-draw", "ellipse 248,162 4,3 0 360 ellipse 264,162 4,3 0 360",
        
        # Mustache (Salt & pepper graying mustache)
        "-fill", "#555555",
        "-draw", "ellipse 244,175 18,8 190 350",
        "-draw", "ellipse 268,175 18,8 190 350",
        "-fill", "#888888",
        "-draw", "ellipse 256,176 10,6 0 360",
        
        # Mouth / Gentle smile
        "-stroke", "#884422", "-strokewidth", "2", "-fill", "none",
        "-draw", "arc 238,175 274,188 10 170",
        "-stroke", "none",
        
        # 7. Rent Receipt Document on Right Foreground
        # White sheet with slight rotation
        "-fill", "#F8FAFC",
        "-draw", "polygon 370,215 460,215 470,340 360,340",
        # Document top fold
        "-fill", "#E2E8F0",
        "-draw", "polygon 445,215 460,215 460,230",
        # Document lines (Blue text bars)
        "-fill", "#60A5FA",
        "-draw", "polygon 378,235 440,235 440,242 378,242",
        "-fill", "#93C5FD",
        "-draw", "polygon 378,252 445,252 445,258 378,258",
        "-draw", "polygon 378,268 420,268 420,274 378,274",
        # Rupee Symbol ₹ on receipt
        "-font", "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
        "-fill", "#0F172A",
        "-pointsize", "48",
        "-annotate", "+405+325", "₹",
        
        # 8. Lower Banner Plaque (Dark Navy with Gold Rim)
        # Upper curved boundary
        "-fill", "#061226",
        "-draw", "polygon 20,350 120,310 256,295 392,310 492,350 492,492 20,492",
        # Double Gold Border on Banner
        "-stroke", "#F59E0B", "-strokewidth", "5", "-fill", "none",
        "-draw", "path 'M 20,350 Q 256,280 492,350'",
        "-stroke", "#FDE047", "-strokewidth", "2", "-fill", "none",
        "-draw", "path 'M 20,356 Q 256,286 492,356'",
        "-stroke", "none",
        
        # Banner Typography:
        # 1. "Market Rent" (Large Bold Display White)
        "-font", "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
        "-fill", "#FFFFFF",
        "-pointsize", "44",
        "-gravity", "north",
        "-annotate", "+0+332", "Market Rent",
        
        # 2. "— MANAGER —" (Golden Yellow Bold)
        "-fill", "#FBBF24",
        "-pointsize", "24",
        "-annotate", "+0+388", "—  M A N A G E R  —",
        
        # 3. "Shops • Rent • Reliable" (Clean White Subtitle)
        "-fill", "#E2E8F0",
        "-pointsize", "18",
        "-annotate", "+0+425", "Shops  •  Rent  •  Reliable",
        
        # 9. Outer Metallic Gold Circular Ring Frame
        "-gravity", "center",
        "-stroke", "#B45309", "-strokewidth", "16", "-fill", "none",
        "-draw", "circle 256,256 256,12",
        "-stroke", "#F59E0B", "-strokewidth", "10", "-fill", "none",
        "-draw", "circle 256,256 256,12",
        "-stroke", "#FEF08A", "-strokewidth", "4", "-fill", "none",
        "-draw", "circle 256,256 256,14",
        "-stroke", "#78350F", "-strokewidth", "2", "-fill", "none",
        "-draw", "circle 256,256 256,21",
        
        output_path
    ]
    
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        print("Error generating logo:", res.stderr)
        return False
    print("Logo successfully generated at", output_path)
    return True

if __name__ == "__main__":
    out_dir = "/app/applet/app/src/main/res/drawable"
    os.makedirs(out_dir, exist_ok=True)
    out_file = os.path.join(out_dir, "market_logo.png")
    generate_market_logo(out_file, 512)
