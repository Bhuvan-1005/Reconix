"""Quick Gemini API diagnostic — run: python test_gemini.py"""
import warnings
warnings.filterwarnings("ignore")

import tempfile, os, sys

print("--- Gemini SDK test ---")

import google.generativeai as genai
print(f"SDK version: {getattr(genai, '__version__', 'unknown')}")

API_KEY = "AIzaSyAVJ37fZ5eNJIbjIIMv_7g-V69CWpiUQr4"
genai.configure(api_key=API_KEY)
print("configure: OK")

# Create a minimal valid PDF
tmp = tempfile.NamedTemporaryFile(suffix=".pdf", delete=False)
# Minimal 1-page PDF with hello text
tmp.write(b"""%PDF-1.4
1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj
2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj
3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R/Resources<<>>/Contents 4 0 R>>endobj
4 0 obj<</Length 44>>
stream
BT /F1 12 Tf 100 700 Td (Hello Invoice) Tj ET
endstream
endobj
xref
0 5
0000000000 65535 f
0000000009 00000 n
0000000058 00000 n
0000000115 00000 n
0000000274 00000 n
trailer<</Size 5/Root 1 0 R>>
startxref
368
%%EOF""")
tmp.close()
print(f"Temp PDF: {tmp.name} ({os.path.getsize(tmp.name)} bytes)")

try:
    uploaded = genai.upload_file(tmp.name, mime_type="application/pdf")
    print(f"upload_file: OK  -> uri={uploaded.uri}")
except Exception as e:
    print(f"upload_file FAILED: {type(e).__name__}: {e}")
    import traceback; traceback.print_exc()
    sys.exit(1)
finally:
    os.unlink(tmp.name)

try:
    model = genai.GenerativeModel(
        model_name="gemini-1.5-flash",
        generation_config=genai.GenerationConfig(
            response_mime_type="application/json",
            temperature=0.0,
        ),
    )
    response = model.generate_content([uploaded, "Extract vendor name from this document. Return JSON: {\"vendor\": \"...\"}"])
    print(f"generate_content: OK")
    print(f"Response: {response.text}")
except Exception as e:
    print(f"generate_content FAILED: {type(e).__name__}: {e}")
    import traceback; traceback.print_exc()
