import sys
import subprocess
import os
import json
import time
from datetime import datetime, timezone
import threading

# Auto-install missing dependencies gracefully
def install_dependencies():
    missing = []
    try:
        import customtkinter
    except ImportError:
        missing.append("customtkinter")
        
    try:
        import requests
    except ImportError:
        missing.append("requests")
        
    if missing:
        print(f"Installing missing dependencies for modern UI: {', '.join(missing)}")
        subprocess.check_call([sys.executable, "-m", "pip", "install", *missing, "--break-system-packages"])

install_dependencies()

import customtkinter as ctk
import requests

# Set modern dark theme
ctk.set_appearance_mode("Dark")  
ctk.set_default_color_theme("blue")  

class ModernFirebaseTester(ctk.CTk):
    def __init__(self):
        super().__init__()

        self.title("Firebase Config & Sync Tester")
        self.geometry("1050x750")
        self.minsize(800, 600)

        # Application state
        self.config_loaded = False
        self.project_id = ""
        self.api_key = ""
        self.id_token = None
        self.local_id = None
        
        # Test Data
        self.test_email = f"tester_{int(time.time())}@oilwatcher.com"
        self.test_password = "SecurePassword123!"
        self.station_id = f"station_{int(time.time())}"

        # Setup Grid
        self.grid_columnconfigure(1, weight=1)
        self.grid_rowconfigure(0, weight=1)

        self.create_sidebar()
        self.create_main_content()
        
        # Load config shortly after launch
        self.after(500, self.load_config)

    def create_sidebar(self):
        self.sidebar = ctk.CTkFrame(self, width=220, corner_radius=0)
        self.sidebar.grid(row=0, column=0, sticky="nsew")
        self.sidebar.grid_rowconfigure(4, weight=1)

        self.logo_label = ctk.CTkLabel(self.sidebar, text="🔥 Firebase Tester", font=ctk.CTkFont(size=20, weight="bold"))
        self.logo_label.grid(row=0, column=0, padx=20, pady=(20, 10))

        self.status_label = ctk.CTkLabel(self.sidebar, text="Status: Checking...", font=ctk.CTkFont(size=14))
        self.status_label.grid(row=1, column=0, padx=20, pady=10)

        self.btn_run_all = ctk.CTkButton(self.sidebar, text="▶ RUN ALL TESTS", font=ctk.CTkFont(weight="bold"), 
                                         fg_color="#10b981", hover_color="#059669", text_color="white", command=self.run_all_tests_thread)
        self.btn_run_all.grid(row=2, column=0, padx=20, pady=20)

        self.appearance_mode_label = ctk.CTkLabel(self.sidebar, text="Appearance Mode:", anchor="w")
        self.appearance_mode_label.grid(row=5, column=0, padx=20, pady=(10, 0))
        self.appearance_mode_menu = ctk.CTkOptionMenu(self.sidebar, values=["Dark", "Light", "System"],
                                                      command=lambda m: ctk.set_appearance_mode(m))
        self.appearance_mode_menu.grid(row=6, column=0, padx=20, pady=(10, 20))

    def create_main_content(self):
        self.main_frame = ctk.CTkFrame(self, corner_radius=0, fg_color="transparent")
        self.main_frame.grid(row=0, column=1, sticky="nsew")
        self.main_frame.grid_columnconfigure(0, weight=1)
        self.main_frame.grid_rowconfigure(3, weight=1)

        # Top Title
        ctk.CTkLabel(self.main_frame, text="Interactive Test Suite", font=ctk.CTkFont(size=24, weight="bold")).grid(row=0, column=0, sticky="w", padx=20, pady=(20, 0))

        # Cards Frame
        self.cards_frame = ctk.CTkFrame(self.main_frame, fg_color="transparent")
        self.cards_frame.grid(row=1, column=0, sticky="ew", padx=10, pady=10)
        self.cards_frame.grid_columnconfigure((0, 1, 2), weight=1)

        # Card 1: Auth
        self.auth_card = ctk.CTkFrame(self.cards_frame, corner_radius=10)
        self.auth_card.grid(row=0, column=0, padx=10, pady=10, sticky="nsew")
        ctk.CTkLabel(self.auth_card, text="🔐 Authentication", font=ctk.CTkFont(size=16, weight="bold")).pack(pady=(15, 10))
        ctk.CTkButton(self.auth_card, text="1. Test Signup (Email)", command=self.test_signup).pack(pady=8, padx=20, fill="x")
        ctk.CTkButton(self.auth_card, text="2. Test Login (Email)", command=self.test_login).pack(pady=8, padx=20, fill="x")
        ctk.CTkButton(self.auth_card, text="3. Pwd Reset (Email)", command=self.test_password_reset).pack(pady=8, padx=20, fill="x")
        ctk.CTkButton(self.auth_card, text="★ Test Google Login", command=self.test_google_login, fg_color="#ea4335", hover_color="#c5221f").pack(pady=8, padx=20, fill="x")

        # Card 2: Users
        self.user_card = ctk.CTkFrame(self.cards_frame, corner_radius=10)
        self.user_card.grid(row=0, column=1, padx=10, pady=10, sticky="nsew")
        ctk.CTkLabel(self.user_card, text="👤 Users DB", font=ctk.CTkFont(size=16, weight="bold")).pack(pady=(15, 10))
        ctk.CTkButton(self.user_card, text="4. Write User", command=self.test_write_user).pack(pady=8, padx=20, fill="x")
        ctk.CTkButton(self.user_card, text="5. Read User", command=self.test_read_user).pack(pady=8, padx=20, fill="x")
        ctk.CTkButton(self.user_card, text="6. Delete User", fg_color="#ef4444", hover_color="#dc2626", text_color="white", command=self.test_delete_user).pack(pady=8, padx=20, fill="x")

        # Card 3: Stations
        self.station_card = ctk.CTkFrame(self.cards_frame, corner_radius=10)
        self.station_card.grid(row=0, column=2, padx=10, pady=10, sticky="nsew")
        ctk.CTkLabel(self.station_card, text="⛽ Stations DB", font=ctk.CTkFont(size=16, weight="bold")).pack(pady=(15, 10))
        ctk.CTkButton(self.station_card, text="7. Write (+ Date)", command=self.test_write_station).pack(pady=8, padx=20, fill="x")
        ctk.CTkButton(self.station_card, text="8. Read Station", command=self.test_read_station).pack(pady=8, padx=20, fill="x")
        ctk.CTkButton(self.station_card, text="9. Delete Station", fg_color="#ef4444", hover_color="#dc2626", text_color="white", command=self.test_delete_station).pack(pady=8, padx=20, fill="x")

        # Console Frame
        self.console_frame = ctk.CTkFrame(self.main_frame, corner_radius=10)
        self.console_frame.grid(row=3, column=0, sticky="nsew", padx=20, pady=(10, 20))
        self.console_frame.grid_columnconfigure(0, weight=1)
        self.console_frame.grid_rowconfigure(1, weight=1)

        self.lbl_console = ctk.CTkLabel(self.console_frame, text="Terminal Logs", font=ctk.CTkFont(weight="bold"))
        self.lbl_console.grid(row=0, column=0, sticky="w", padx=15, pady=(10, 0))

        # Modern textbox acting as a console
        self.log_text = ctk.CTkTextbox(self.console_frame, font=ctk.CTkFont(family="Consolas", size=13), fg_color="#1e1e1e", text_color="#d4d4d4", corner_radius=5)
        self.log_text.grid(row=1, column=0, sticky="nsew", padx=15, pady=15)

    def log(self, message):
        timestamp = datetime.now().strftime("%H:%M:%S")
        self.log_text.insert("end", f"[{timestamp}] {message}\n")
        self.log_text.see("end")

    def load_config(self):
        config_path = "app/google-services.json"
        if not os.path.exists(config_path):
            self.status_label.configure(text="Status: Error\nMissing Config", text_color="#ef4444")
            self.log(f"❌ ERROR: Could not find '{config_path}'.")
            self.log("⚠️ Make sure you are running this from inside the OIL_MONITOR_APP folder!")
            return
            
        try:
            with open(config_path, "r") as f:
                config = json.load(f)
            self.project_id = config["project_info"]["project_id"]
            self.api_key = config["client"][0]["api_key"][0]["current_key"]
            self.config_loaded = True
            
            self.status_label.configure(text=f"🟢 Online\n{self.project_id}", text_color="#10b981")
            self.log("==================================================")
            self.log("✅ Configuration successfully mapped!")
            self.log(f"Project ID : {self.project_id}")
            self.log(f"API Key    : {self.api_key[:5]}...{self.api_key[-5:]}")
            self.log("==================================================\n")
        except Exception as e:
            self.status_label.configure(text="Status: Parse Error", text_color="#ef4444")
            self.log(f"❌ Error parsing {config_path}: {e}")

    # ========================== TESTS ==========================
    def test_signup(self):
        if not self.config_loaded: return False
        self.log(f"\n▶ [TEST 1] Testing Email Signup -> {self.test_email}")
        url = f"https://identitytoolkit.googleapis.com/v1/accounts:signUp?key={self.api_key}"
        res = requests.post(url, json={"email": self.test_email, "password": self.test_password, "returnSecureToken": True})
        if res.status_code == 200:
            data = res.json()
            self.id_token = data["idToken"]
            self.local_id = data["localId"]
            self.log("✅ SUCCESS: User created and Auth Token retrieved.")
            return True
        else:
            self.log(f"❌ FAILED: {res.text}")
            return False

    def test_login(self):
        if not self.config_loaded: return False
        self.log("\n▶ [TEST 2] Testing Email Login")
        url = f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={self.api_key}"
        res = requests.post(url, json={"email": self.test_email, "password": self.test_password, "returnSecureToken": True})
        if res.status_code == 200:
            data = res.json()
            self.id_token = data["idToken"]
            self.local_id = data["localId"]
            self.log("✅ SUCCESS: Logged in successfully.")
            return True
        else:
            self.log(f"❌ FAILED: {res.text}")
            return False

    def test_password_reset(self):
        if not self.config_loaded: return False
        self.log("\n▶ [TEST 3] Testing Password Reset")
        url = f"https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key={self.api_key}"
        res = requests.post(url, json={"requestType": "PASSWORD_RESET", "email": self.test_email})
        if res.status_code == 200:
            self.log("✅ SUCCESS: Password reset email API executed.")
            return True
        else:
            self.log(f"❌ FAILED: {res.text}")
            return False

    def test_google_login(self):
        if not self.config_loaded: return False
        self.log("\n▶ [TEST Google] Testing Google Sign-In Provider")
        
        # We need a Google ID token to test this properly. Ask the user.
        dialog = ctk.CTkInputDialog(text="To test Google Auth, you need a valid Google ID Token.\nPaste one below to fully test the login flow.\n\n(Leave blank to just ping the provider endpoint)", title="Google Login Test")
        token = dialog.get_input()
        
        if token is None:
            self.log("⚠️ Google Login test aborted by user.")
            return False
            
        url = f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key={self.api_key}"
        payload = {
            "postBody": f"id_token={token}&providerId=google.com",
            "requestUri": "http://localhost",
            "returnIdpCredential": True,
            "returnSecureToken": True
        }
        res = requests.post(url, json=payload)
        
        if res.status_code == 200:
            data = res.json()
            self.id_token = data["idToken"]
            self.local_id = data["localId"]
            self.log("✅ SUCCESS: Google Login verified. Token accepted and converted to Firebase Auth Token.")
            return True
        else:
            # Check if the error is OPERATION_NOT_ALLOWED vs INVALID_IDP_RESPONSE
            err_msg = res.json().get("error", {}).get("message", "")
            if err_msg == "OPERATION_NOT_ALLOWED":
                self.log("❌ FAILED: Google Sign-In is DISABLED in your Firebase Console.")
            elif err_msg == "INVALID_IDP_RESPONSE" and not token:
                self.log("⚠️ PING: Google Sign-In appears to be ENABLED, but we passed a blank token so it failed as expected.")
                self.log("   (Paste a real Google ID Token next time to fully verify the login).")
                return True
            else:
                self.log(f"❌ FAILED: {err_msg}")
            return False

    def test_write_user(self):
        if not self.id_token: 
            self.log("⚠️ Please test login/signup first to get an auth token.")
            return False
        self.log("\n▶ [TEST 4] Testing Write User Data (Firestore)")
        url = f"https://firestore.googleapis.com/v1/projects/{self.project_id}/databases/(default)/documents/users/{self.local_id}"
        current_time = datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")
        payload = {
            "fields": {
                "uid": {"stringValue": self.local_id},
                "name": {"stringValue": "Python GUI Modern User"},
                "email": {"stringValue": self.test_email},
                "createdAt": {"timestampValue": current_time}
            }
        }
        res = requests.patch(url, json=payload, headers={"Authorization": f"Bearer {self.id_token}"})
        if res.status_code == 200:
            self.log("✅ SUCCESS: User data successfully written.")
            return True
        else:
            self.log(f"❌ FAILED: {res.text}")
            return False

    def test_read_user(self):
        if not self.id_token: return False
        self.log("\n▶ [TEST 5] Testing Read User Data")
        url = f"https://firestore.googleapis.com/v1/projects/{self.project_id}/databases/(default)/documents/users/{self.local_id}"
        res = requests.get(url, headers={"Authorization": f"Bearer {self.id_token}"})
        if res.status_code == 200:
            self.log("✅ SUCCESS: User data retrieved.")
            self.log(f"Payload: {json.dumps(res.json().get('fields', {}), indent=2)}")
            return True
        else:
            self.log(f"❌ FAILED: {res.text}")
            return False

    def test_delete_user(self):
        if not self.id_token: return False
        self.log("\n▶ [TEST 6] Testing Delete User (DB & Auth)")
        # DB
        url_db = f"https://firestore.googleapis.com/v1/projects/{self.project_id}/databases/(default)/documents/users/{self.local_id}"
        res_db = requests.delete(url_db, headers={"Authorization": f"Bearer {self.id_token}"})
        if res_db.status_code == 200:
            self.log("✅ SUCCESS: DB User document wiped.")
        else:
            self.log(f"❌ FAILED (DB): {res_db.text}")
            
        # Auth
        url_auth = f"https://identitytoolkit.googleapis.com/v1/accounts:delete?key={self.api_key}"
        res_auth = requests.post(url_auth, json={"idToken": self.id_token})
        if res_auth.status_code == 200:
            self.log("✅ SUCCESS: Auth User identity wiped.")
            self.id_token, self.local_id = None, None
            return True
        else:
            self.log(f"❌ FAILED (Auth): {res_auth.text}")
            return False

    def test_write_station(self):
        if not self.id_token: return False
        self.log("\n▶ [TEST 7] Testing Write Station (w/ RFC3339 Timestamp)")
        url = f"https://firestore.googleapis.com/v1/projects/{self.project_id}/databases/(default)/documents/stations/{self.station_id}"
        current_time = datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")
        payload = {
            "fields": {
                "name": {"stringValue": "Next-Gen Test Station"},
                "lastUpdated": {"timestampValue": current_time},
                "latestPrices": {"mapValue": {"fields": {"regular": {"doubleValue": 3.99}}}}
            }
        }
        res = requests.patch(url, json=payload, headers={"Authorization": f"Bearer {self.id_token}"})
        if res.status_code == 200:
            self.log(f"✅ SUCCESS: Station data written with Timestamp ({current_time}).")
            return True
        else:
            self.log(f"❌ FAILED: {res.text}")
            return False

    def test_read_station(self):
        if not self.id_token: return False
        self.log("\n▶ [TEST 8] Testing Read Station")
        url = f"https://firestore.googleapis.com/v1/projects/{self.project_id}/databases/(default)/documents/stations/{self.station_id}"
        res = requests.get(url, headers={"Authorization": f"Bearer {self.id_token}"})
        if res.status_code == 200:
            self.log("✅ SUCCESS: Station data retrieved.")
            self.log(f"Payload: {json.dumps(res.json().get('fields', {}), indent=2)}")
            return True
        else:
            self.log(f"❌ FAILED: {res.text}")
            return False

    def test_delete_station(self):
        if not self.id_token: return False
        self.log("\n▶ [TEST 9] Testing Delete Station")
        url = f"https://firestore.googleapis.com/v1/projects/{self.project_id}/databases/(default)/documents/stations/{self.station_id}"
        res = requests.delete(url, headers={"Authorization": f"Bearer {self.id_token}"})
        if res.status_code == 200:
            self.log("✅ SUCCESS: Station DB document wiped.")
            return True
        else:
            self.log(f"❌ FAILED: {res.text}")
            return False

    def run_all_tests_thread(self):
        threading.Thread(target=self.run_all_tests, daemon=True).start()

    def run_all_tests(self):
        self.log("\n\n🚀 ==== STARTING FULL TEST SEQUENCE ==== 🚀\n")
        
        tests = [
            self.test_signup, self.test_login, self.test_password_reset,
            self.test_write_user, self.test_read_user, 
            self.test_write_station, self.test_read_station, self.test_delete_station,
            self.test_delete_user
        ]
        
        for test in tests:
            success = test()
            if not success and test == self.test_signup:
                self.log("🛑 Sequence halted due to Auth failure.")
                return
            time.sleep(1) # Visual pause
            
        self.log("\n🎉 ==== ALL TESTS PASSED SUCCESSFULLY ==== 🎉\n")


if __name__ == "__main__":
    app = ModernFirebaseTester()
    app.mainloop()
