import SwiftUI
import ios_tuvali_library

struct ContentView: View {
    
    var wallet = Wallet()
    
    var body: some View {
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text("Hello, world!")
                .onAppear {
                
                    wallet.startConnection("https://www.google.co.in")
                    wallet.disconnect()
                }
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        // The ContentView preview will now include the function execution in onAppear
        ContentView()
    }
}

