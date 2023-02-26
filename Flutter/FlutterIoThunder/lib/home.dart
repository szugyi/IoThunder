import 'dart:async';
import 'dart:developer' as developer;

import 'package:flutter/material.dart';
import 'package:flutter_colorpicker/flutter_colorpicker.dart';
import 'package:http/http.dart' as http;

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  Timer? _debounceTimer;
  Color currentColor = Colors.amber.withAlpha(5);

  @override
  void dispose() {
    _debounceTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("IoThunder"),
      ),
      body: Center(
        child: Column(
          children: <Widget>[
            buildColorRow(context),
          ],
        ),
      ),
    );
  }

  Widget buildColorRow(BuildContext context) {
    return GestureDetector(
      behavior: HitTestBehavior.translucent,
      onTap: () {
        _showColorPickerDialog();
      },
      child: Row(
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: SizedBox(
              width: 42.0,
              height: 42.0,
              child: DecoratedBox(
                decoration: BoxDecoration(color: currentColor.withAlpha(255)),
              ),
            ),
          ),
          Text(
            "Color: ${currentColor.red}, ${currentColor.green}, ${currentColor.blue} | Alpha: ${currentColor.alpha}",
            style: Theme.of(context).textTheme.headline5,
          ),
        ],
      ),
    );
  }

  Future<void> _showColorPickerDialog() async {
    void changeColor(Color color) {
      setState(() {
        currentColor = color;
        _setRemoteColorDebounced();
      });
    }

    return showDialog<void>(
      context: context,
      barrierDismissible: false, // user must tap button!
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Pick a color!'),
          content: SingleChildScrollView(
            child: ColorPicker(
              pickerColor: currentColor,
              onColorChanged: changeColor,
              enableAlpha: true,
            ),
          ),
          actions: <Widget>[
            ElevatedButton(
              child: const Text('Done'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  void debouncing({required Function() fn, int waitForMs = 500}) {
    if (_debounceTimer == null) {
      fn();

      _debounceTimer = Timer(Duration(milliseconds: waitForMs), () {
        fn();
        _debounceTimer = null;
      });
    }
  }

  void _setRemoteColorDebounced() {
    debouncing(fn: () {
      _setRemoteColor(currentColor);
    });
  }

  Future<void> _setRemoteColor(Color color) async {
    var response = await http.post(
      Uri.parse('http://192.168.88.236/color'),
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      body: {
        'red': color.red.toString(),
        'green': color.green.toString(),
        'blue': color.blue.toString(),
        'alpha': color.alpha.toString()
      },
    );

    developer.log(response.body);
  }
}
