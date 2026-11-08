
import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import 'native/canaux.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'TP3 Platform Channels',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      home: const PageCanaux(),
    );
  }
}

class PageCanaux extends StatefulWidget {
  const PageCanaux({super.key});

  @override
  State<PageCanaux> createState() => _PageCanauxState();
}

class _PageCanauxState extends State<PageCanaux> {
  String _message = 'Appuyez sur un bouton';
  bool _audioAutorise = false;

  Future<void> _demanderMicro() async {
    try {
      final autorise = await CanauxNatifs.demanderPermissionAudio();

      setState(() {
        _audioAutorise = autorise;
        _message = autorise
            ? 'Permission microphone accordée'
            : 'Permission microphone refusée';
      });
    } on PlatformException catch (e) {
      setState(() {
        _message = 'Erreur : ${e.message}';
      });
    }
  }

  Future<void> _lireInfos() async {
    try {
      final infos = await CanauxNatifs.lireInfos();

      setState(() {
        _message = '''
Fabricant : ${infos['fabricant']}
Modèle : ${infos['modele']}
Version Android : ${infos['version']}
SDK : ${infos['sdk']}
''';
      });
    } catch (e) {
      setState(() => _message = 'Erreur : $e');
    }
  }

  Future<void> _vibrer(String motif) async {
    try {
      final resultat = await CanauxNatifs.vibrer(motif);
      setState(() => _message = resultat);
    } on PlatformException catch (e) {
      setState(() => _message = '${e.code} : ${e.message}');
    }
  }

  Widget _bouton(String texte, VoidCallback action) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 5),
      child: ElevatedButton(
        onPressed: action,
        child: Text(texte),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('TP3 - Platform Channels'),
        backgroundColor: Colors.blue,
        foregroundColor: Colors.white,
      ),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.phone_android, size: 60),
              const SizedBox(height: 15),

              const Text(
                'Informations de l’appareil',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
              _bouton('Lire les informations', _lireInfos),

              const Divider(height: 30),

              const Text(
                'Test du vibreur',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
              _bouton('Vibration courte', () => _vibrer('court')),
              _bouton('Vibration longue', () => _vibrer('long')),
              _bouton('Vibration SOS', () => _vibrer('sos')),

              const Divider(height: 30),

              const Text(
                'Capteur de lumière',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 15),

              StreamBuilder<double>(
                stream: CanauxNatifs.ecouterLumiere(),
                builder: (context, snapshot) {
                  if (snapshot.hasError) {
                    return Text('Erreur : ${snapshot.error}');
                  }

                  if (!snapshot.hasData) {
                    return const Text('En attente du capteur...');
                  }

                  return Text(
                    'Luminosité : ${snapshot.data!.toStringAsFixed(2)} lux',
                    style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  );
                },
              ),

              const Divider(height: 30),

              const Text(
                'Niveau sonore',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 10),

              ElevatedButton(
                onPressed: _demanderMicro,
                child: const Text('Autoriser le microphone'),
              ),

              if (_audioAutorise)
                StreamBuilder<double>(
                  stream: CanauxNatifs.ecouterNiveauAudio(),
                  builder: (context, snapshot) {
                    if (snapshot.hasError) {
                      return Text('Erreur : ${snapshot.error}');
                    }

                    if (!snapshot.hasData) {
                      return const Text('En attente du microphone...');
                    }

                    return Text(
                      'Niveau sonore : ${snapshot.data!.toStringAsFixed(2)} dBFS',
                      style: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    );
                  },
                ),

              const SizedBox(height: 20),

              Text(
                _message,
                textAlign: TextAlign.center,
                style: const TextStyle(fontSize: 16),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
