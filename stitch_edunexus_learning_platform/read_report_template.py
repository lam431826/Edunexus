import zipfile
import xml.etree.ElementTree as ET
import os
import sys

docx_path = r"d:\Gx-RollNumber_AI_Usage_Report.docx"
output_path = r"D:\stitch_edunexus_learning_platform (1)\stitch_edunexus_learning_platform\report_template_text.txt"

print(f"Reading {docx_path}...")
if not os.path.exists(docx_path):
    print(f"Error: File not found at {docx_path}")
    sys.exit(1)

try:
    with zipfile.ZipFile(docx_path) as z:
        doc_xml = z.read("word/document.xml")
        root = ET.fromstring(doc_xml)
        
        # Namespace map for word xml tags
        ns = {
            'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
        }
        
        paragraphs = []
        for p in root.findall('.//w:p', ns):
            p_text = "".join([t.text for t in p.findall('.//w:t', ns) if t.text])
            if p_text.strip():
                paragraphs.append(p_text)
                
        with open(output_path, "w", encoding="utf-8") as f:
            f.write("=== PARAGRAPHS ===\n")
            for idx, p in enumerate(paragraphs):
                f.write(f"Para {idx}: {p}\n")
                
        print(f"Success! Extracted {len(paragraphs)} paragraphs to {output_path}")
except Exception as e:
    print(f"Error: {e}")
